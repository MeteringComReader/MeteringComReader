/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import java.sql.Date;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author Juliusz
 */
public class TestFlashSession {
        public static void main(String args[]) throws MeteringSessionException, InterruptedException{
            PropertyConfigurator.configure(TestFlashSession.class.getResource("log4j.properties"));
     
            Hubs hubs = HubSessionManager.discoverHubs();
            Hub hub=null;
            
            Iterator<Map.Entry<String, Hub>> it =hubs.entrySet().iterator();
            if (it.hasNext()) {
                Map.Entry<String, Hub> pairs = (Map.Entry<String, Hub>)it.next();
                hub = pairs.getValue();

            }
            else{
                throw new MeteringSessionException("nie znaleziono żadnego huba");
            }
            HubConnection hc=HubConnection.createHubConnection(hub);
            /*
            HubFlashSession hubSession = hc.createHubFlashSession(new Timestamp(0));
            DataPacket packet;
            while ((packet = hubSession.getNextPacket())!=null){
            ;
            }
            hubSession.close();
             */
            LoggerFlashSession loggerFlashSession = hc.createLoggerFlashSession(new Timestamp(0));
            DataPacket packet;
            
                    
/*           
           while ((packet = loggerFlashSession.getNextPacket())!=null){
                System.out.println(packet);
           }
  */        
            
            hc.close();
            
            
        }
    
}
