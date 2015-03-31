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
import java.util.logging.Level;
import org.apache.log4j.PropertyConfigurator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Juliusz
 */
public class HubSessionDBManager extends HubSessionManager {
    /**
     * Utworzenie loggera systemowego
     */
    private static final Logger lgr = LoggerFactory.getLogger(HubSessionDBManager.class);




    protected HubSessionDBManager() throws MeteringSessionException {
        super();        
    }
    

            
     public static HubSessionDBManager getHubSessionManager(){
         if (hbs==null)
             try {
             hbs=new HubSessionDBManager();
         } catch (MeteringSessionException ex) {
             throw new RuntimeException(ex);
         }
         return (HubSessionDBManager)hbs;
     }


    @Override
    public void run() {
        while(isRunHubSessionManager()){
            
            try{
                Hubs hubs = discoverHubs();
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

    
    @Override
    protected  void startHubSessionAndRS(String hubNo) throws MeteringSessionException{
        HubConnection hc = connectHubAndStartRS(hubNo, 61); //TODO 1)przekazać parametr do wywołania(?), 2)zmienić timeout
        RadioSessionDBInserter sessionInserter = RadioSessionDBInserter.createRadioSessionDBInserter(hc);        
        sessionInserter.mainThread(); 
        radioInserters.addInserter(hubNo, sessionInserter);
    }
        
    public static void main(String args[]) throws MeteringSessionException, InterruptedException{
        
     PropertyConfigurator.configure(HubSessionDBManager.class.getResource("log4j.properties"));
        
        
        HubSessionManager hbs= HubSessionDBManager.getHubSessionManager();
        hbs.startHubSessionManager();
        hbs.addShutdownHook();
//        Thread.sleep(1000*60);

//        HubSessionManager.downloadMeasurmentsFromLogger("4D4503000000", null);
        
//        Thread.sleep(1000*60*300);
        

    }
}
