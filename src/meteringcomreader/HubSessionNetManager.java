/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import meteringcomreader.exceptions.MeteringSessionSPException;
import meteringcomreader.exceptions.MeteringSessionException;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Level;
import org.apache.log4j.PropertyConfigurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Juliusz
 */
public class HubSessionNetManager extends HubSessionManager {
    /**
     * Utworzenie loggera systemowego
     */
    private static final Logger lgr = LoggerFactory.getLogger(HubSessionNetManager.class);




    protected HubSessionNetManager() throws MeteringSessionException {
        super();        
    }
    

            
     public static HubSessionNetManager getHubSessionManager(){
         if (hbs==null)
             try {
             hbs=new HubSessionNetManager();
         } catch (MeteringSessionException ex) {
             throw new RuntimeException(ex);
         }
         return (HubSessionNetManager)hbs;
     }


    @Override
    public void run() {
        while(isRunHubSessionManager()){
            
            try{
                Hubs hubs = discoverHubs();
                startHubSessionAndRS(hubs);
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

    
    @Override
    protected  void startHubSessionAndRS(String hubNo) throws MeteringSessionException{
        HubConnection hc = connectHubAndStartRS(hubNo, 61); //TODO 1)przekazać parametr do wywołania(?), 2)zmienić timeout
//        RadioSessionDBInserter sessionInserter = RadioSessionDBInserter.createRadioSessionDBInserter(hc);        
//        sessionInserter.mainThread(); 
//        radioInserters.addInserter(hubNo, sessionInserter);
    }
    
    public void addNetInserterForAllHubs(ObjectOutputStream oos) throws MeteringSessionException{
        HubsSessions hubsSess = getHubsSessions();

        Set<Map.Entry<String, HubConnection>> connectionSet= hubsSess.entrySet();
        Iterator<Map.Entry<String, HubConnection>> it = connectionSet.iterator();
        while(it.hasNext()){
            Map.Entry<String, HubConnection> pair= it.next();
            HubConnection hc= pair.getValue();
            SessionStreamInserter inserter =  new SessionStreamInserter(hc, oos);
            radioInserters.addInserter(hc.getHub().getHubHexId(), inserter);
            inserter.mainThread();
        }        
    }
        
    public static void main(String args[]) throws MeteringSessionException, InterruptedException, FileNotFoundException, IOException{
        
     PropertyConfigurator.configure(HubSessionNetManager.class.getResource("log4j.properties"));
        
        
        HubSessionNetManager hbs= HubSessionNetManager.getHubSessionManager();
        hbs.startHubSessionManager();
        hbs.addShutdownHook();
        
        Thread.sleep(1000*20);

        FileOutputStream fout = new FileOutputStream("c:\\temp\\dp.ser");
        ObjectOutputStream oos = new ObjectOutputStream(fout);
        hbs.addNetInserterForAllHubs(oos);
        
        

//        HubSessionManager.downloadMeasurmentsFromLogger("4D4503000000", null);
        
//        Thread.sleep(1000*60*300);
        

    }
}
