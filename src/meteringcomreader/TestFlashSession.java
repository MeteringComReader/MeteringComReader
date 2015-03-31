/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import meteringcomreader.exceptions.MeteringSessionTimeoutException;
import meteringcomreader.exceptions.MeteringSessionException;
import java.sql.Date;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import meteringcomreader.exceptions.MeteringSessionOperationAlreadyInProgressException;
import org.apache.log4j.PropertyConfigurator;

/**
 *
 * @author Juliusz
 */
public class TestFlashSession {
        public static void main(String args[]) throws MeteringSessionException, InterruptedException, Exception{
            PropertyConfigurator.configure(TestFlashSession.class.getResource("log4j.properties"));
     
            /* TODO start latka do pominiecia ostatniej strony pamieci loggera*/
            /* zmienić w projekcie główna klasę! */
            if (args.length>0 && "-skipLastPage".equals(args[0]))
                LoggerFlashSession.skipLastPage=true;
            else
                LoggerFlashSession.skipLastPage=false;
            System.out.println("skipLastPage="+Boolean.toString(LoggerFlashSession.skipLastPage));            
            /* TODO end latka do pominiecia ostatniej strony pamieci loggera*/
            
             HubConnection hc=null;
            try{
            Hubs hubs = HubSessionDBManager.getHubSessionManager().discoverHubs();
            Hub hub=null;
            
            Iterator<Map.Entry<String, Hub>> it =hubs.entrySet().iterator();
            if (it.hasNext()) {
                Map.Entry<String, Hub> pairs = (Map.Entry<String, Hub>)it.next();
                hub = pairs.getValue();

            }
            else{
                throw new MeteringSessionException("nie znaleziono żadnego huba");
            }
             hc=HubConnection.createHubConnection(hub);
            /*
            HubFlashSession hubSession = hc.createHubFlashSession(new Timestamp(0));
            DataPacket packet;
            while ((packet = hubSession.getNextPacket())!=null){
            ;
            }
            hubSession.close();
             */

            
            LoggerFlashSession loggerFlashSession = hc.createLoggerFlashSession(new Timestamp(0));
            DataPacket packet=null;
            
            while ((packet = loggerFlashSession.getNextPacket(100000))!=null){
            System.out.println(packet);
            }
            
/*            
            LoggerFlashSessionDBInserter loggerFlashSessionDBInserter = LoggerFlashSessionDBInserter.createLoggerFlashSessionDBInserter(hc, null);
            loggerFlashSessionDBInserter.mainThread();
*/            
            
//            HubSessionManager.downloadMeasurmentsFromLogger(hub.getHubHexId(), null);
            
            }
            finally{
                if (hc!=null)
                    hc.close();
            }
            
            
        }
    
}
