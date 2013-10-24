/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import meteringcomreader.exceptions.MeteringSessionSPException;
import meteringcomreader.exceptions.MeteringSessionException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import org.apache.log4j.PropertyConfigurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Juliusz
 */
public class HubSessionManager implements Runnable {
    /**
     * Utworzenie loggera systemowego
     */
    private static final Logger lgr = LoggerFactory.getLogger(HubSessionManager.class);


    static protected Hubs discoveredHubs;
    static protected HubsSessions hubsSessions = new HubsSessions(10);
    
    static protected HubSessionManager hbs=null;


    public static void registerTestHub(Hub hub){
        HubConnection hc = HubConnection.createEmptyHubConnection(hub);
        getHubsSessions().put(hub.getHubHexId(), hc);
    }
    
    public static void unregisterTestHub(Hub hub){
        getHubsSessions().remove(hub.getHubHexId());
    }
            
    public static boolean isRegisteredForDCN(long regId) {
        HubsSessions hubsSess = getHubsSessions();

        Set<Map.Entry<String, HubConnection>> connectionSet= hubsSess.entrySet();
        Iterator<Map.Entry<String, HubConnection>> it = connectionSet.iterator();
        while(it.hasNext()){
            Map.Entry<String, HubConnection> pair= it.next();
            HubConnection hc= pair.getValue();
            long registredId = hc.getHub().getDCR().getRegId();
            if (regId==registredId)
                return true;
        }        
        return false;        
    }
    
    protected boolean runHubSessionManager=true;
    protected Thread hubSessionManagerThread=null;
    
        static protected SessionInserters radioInserters=new SessionInserters(10);
//    static protected SessionInserters flashInserters=new SessionInserters(10);


    private HubSessionManager() throws MeteringSessionException {
        if (hbs==null)
            HubSessionManager.hbs=this;
        else
            throw new MeteringSessionException("Hub Session Manager already exists");
    }
    

            
    static public void startHubSessionManager() throws MeteringSessionException
    {
        hbs=new HubSessionManager();
        Thread hbst= new Thread(hbs, "HubSessionManagerThread");
        hbs.setHubSessionManagerThread(hbst);
        hbst.start();
    }

    static public void stopHubSessionManager() throws MeteringSessionException
    {
        if (hbs!=null){
            hbs.setRunHubSessionManager(false);
            hbs.getHubSessionManagerThread().interrupt();
            try {
                hbs.getHubSessionManagerThread().join();
            } catch (InterruptedException ex) {
                //ignore it
            }
            closeAllConnections();
            closeAllInserters();
            Set<Map.Entry<String, Hub>> hubsSet= discoveredHubs.entrySet();
            Iterator<Entry<String, Hub>> it = hubsSet.iterator();
            while(it.hasNext()){
                Entry<String, Hub> pair= it.next();
                Hub hub= pair.getValue();
                SessionDBInserterHelper.unregisterHub(hub);
            }            
            hbs=null;
        }
    }


    
    static public Hubs discoverHubs(){
        discoveredHubs=HubConnection.discoverHubs(getHubsSessions());
        return getDiscoveredHubs();
    }
    
    static public HubConnection connectHubAndStartRS(String hubNo, int timeout) throws MeteringSessionException{        
        Hub hub = getDiscoveredHubs().getHub(hubNo);
        HubConnection hc = HubConnection.createHubConnection(hub);
        getHubsSessions().put(hubNo, hc);
        hc.createRadioSession(timeout); 
        return hc;
    }
    
    static public void closeHubSession(Hub hub){
        try {
            String hubNo = hub.getHubHexId();
            RadioSessionDBInserter inserter = radioInserters.getInserter(hubNo);
            if (inserter!=null)
                inserter.close();
            radioInserters.removeInserter(hubNo);
            HubConnection hc = getHubsSessions().getHubConnection(hubNo);
            if (hc!=null)
                hc.close();
            getHubsSessions().remove(hubNo);
            SessionDBInserterHelper.unregisterHub(hub);
        } catch (MeteringSessionException ex) {
            //ignore it
        }
    }
    
    static public void downloadMeasurmentsFromHub(String hubNo, Timestamp from) throws MeteringSessionException{
        HubConnection hc = getHubsSessions().getHubConnection(hubNo);
        hc.createHubFlashSession(from);
    }

    static public int downloadMeasurmentsFromLogger(String hubNo, Timestamp from) throws MeteringSessionException{
        HubConnection hc = getHubsSessions().getHubConnection(hubNo);
        int newMeasurments = 0;
        hc.closeRadioSession();
        try{
            LoggerFlashSessionDBInserter loggerFlashSessionDBInserter = LoggerFlashSessionDBInserter.createLoggerFlashSessionDBInserter(hc, from);
            newMeasurments = loggerFlashSessionDBInserter.mainThread();                    
        }
        finally{
            hc.closeLoggerFlashSession();
            hc.createRadioSession(61); //TODO zmienić timeout
        }
        return newMeasurments;
    }
    /**
     * @return the discoveredHubs
     */
    public static Hubs getDiscoveredHubs() {
        return discoveredHubs;
    }

    /**
     * @return the hubsSessions
     */
    public static HubsSessions getHubsSessions() {
        return hubsSessions;
    }

    public static void closeAllConnections() {
        Set<Map.Entry<String, HubConnection>> connectionSet= getHubsSessions().entrySet();
        Iterator<Entry<String, HubConnection>> it = connectionSet.iterator();
        while(it.hasNext()){
            Entry<String, HubConnection> pair= it.next();
            HubConnection hc= pair.getValue();
            hc.close();
            it.remove();
//            getHubsSessions().remove(pair.getKey());
        }
lgr.debug("Time:"+System.nanoTime()+","+"is hubsSessionsMap empty "+getHubsSessions().isEmpty());
    }

    private static void closeAllInserters() {
        Set<Map.Entry<String, RadioSessionDBInserter>> insertersSet= radioInserters.entrySet();
        Iterator<Map.Entry<String, RadioSessionDBInserter>> it = insertersSet.iterator();
        while(it.hasNext()){
            Map.Entry<String, RadioSessionDBInserter> pair= it.next();
            RadioSessionDBInserter ins= pair.getValue();
            try {
                ins.close();
            } catch (MeteringSessionException ex) {
                lgr.warn(null, ex);
            }
            it.remove();
//            getHubsSessions().remove(pair.getKey());
        }
 lgr.debug("Time:"+System.nanoTime()+","+"is radioInserters empty "+radioInserters.isEmpty());
    }    

    @Override
    public void run() {
        while(isRunHubSessionManager()){
            
            try{
                Hubs hubs = HubSessionManager.discoverHubs();
                startHubSessionAndRS(hubs);
                meteringcomreader.SessionDBInserterHelper.registerHubs(hubs);
                try {
                    Thread.sleep(1000*20);
                } catch (InterruptedException ex) {
                    //ignore it;
                }
            } catch(MeteringSessionSPException ex){
                    lgr.warn(null, ex);
            } catch(MeteringSessionException ex){
                    lgr.warn(null, ex);
            }
        }
      
    }


    /**
     * @return the hubSessionManagerThread
     */
    public Thread getHubSessionManagerThread() {
        return hubSessionManagerThread;
    }

    /**
     * @param hubSessionManagerThread the hubSessionManagerThread to set
     */
    public void setHubSessionManagerThread(Thread hubSessionManagerThread) {
        this.hubSessionManagerThread = hubSessionManagerThread;
    }

    /**
     * @return the runHubSessionManager
     */
    synchronized public boolean isRunHubSessionManager() {
        return runHubSessionManager;
    }

    /**
     * @param runHubSessionManager the runHubSessionManager to set
     */
    synchronized public void setRunHubSessionManager(boolean runHubSessionManager) {
        this.runHubSessionManager = runHubSessionManager;
    }
    
    protected static void startHubSessionAndRS(String hubNo) throws MeteringSessionException{
        HubConnection hc = HubSessionManager.connectHubAndStartRS(hubNo, 61); //TODO 1)przekazać parametr do wywołania(?), 2)zmienić timeout
        RadioSessionDBInserter sessionInserter = RadioSessionDBInserter.createRadioSessionDBInserter(hc);        
        sessionInserter.mainThread(); 
        radioInserters.addInserter(hubNo, sessionInserter);
    }
    
    protected static void startHubSessionAndRS(Hubs hubs) throws MeteringSessionException{
        Set<Map.Entry<String, Hub>> connectionSet= hubs.entrySet();
        Iterator<Entry<String, Hub>> it = connectionSet.iterator();
        while(it.hasNext()){
            Entry<String, Hub> pair= it.next();
            Hub hub= pair.getValue();
            startHubSessionAndRS(hub.getHubHexId());
        }
    }
    
   public static  void addShutdownHook(){
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {try {
                    lgr.info("stopping hubSessionManager")        ;
                    HubSessionManager.stopHubSessionManager();
                } catch (MeteringSessionException ex) {
                    lgr.warn(null, ex);
                }
              }
            });       
   }

    public static void main(String args[]) throws MeteringSessionException, InterruptedException{
        
     PropertyConfigurator.configure(HubSessionManager.class.getResource("log4j.properties"));
        addShutdownHook();
        
        
        HubSessionManager.startHubSessionManager();

//        Thread.sleep(1000*60);

//        HubSessionManager.downloadMeasurmentsFromLogger("4D4503000000", null);
        
        Thread.sleep(1000*60*300);
        

    }
}
