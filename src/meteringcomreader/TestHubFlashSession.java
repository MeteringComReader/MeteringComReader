/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import meteringcomreader.exceptions.MeteringSessionTimeoutException;
import meteringcomreader.exceptions.MeteringSessionException;
import java.util.Date;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import meteringcomreader.exceptions.MeteringSessionOperationAlreadyInProgressException;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Juliusz Jezierski
 */
public class TestHubFlashSession {
                private static final Logger lgr = LoggerFactory.getLogger(TestHubFlashSession.class);
        public static void main(String args[]) throws MeteringSessionException, InterruptedException, Exception{
            PropertyConfigurator.configure(TestHubFlashSession.class.getResource("log4j.properties"));

     
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
             try{
                hc.closeAllSessions();
             }
             catch(Exception e)
             {
                 System.err.println(e.getMessage());                      
             }
*/
            /*
            HubFlashSession hubSession = hc.createHubFlashSession(new Timestamp(0));
            DataPacket packet;
            while ((packet = hubSession.getNextPacket())!=null){
            ;
            }
            hubSession.close();
             */
           
            int hubHVer=hc.getHubHardwareVersion();
            int hubFVer=hc.getHubFirmawareVersion();
            System.out.println("hubHVer"+Integer.toHexString(hubHVer));
            System.out.println("hubFVer"+Integer.toHexString(hubFVer));
/*  
            Date now =new Date();
            
            long[] loggers = hc.getRegistredLoggers();
            for(int i=0; i< loggers.length; i++){
                System.out.println(Long.toHexString(loggers[i]));
            }
//            hc.setHubTime(new Timestamp((new Date()).getTime()));
            Timestamp time = hc.getHubTime();
            System.out.println(time.toString());
*/
//            hc.createRadioSession(89);

           
            HubFlashSession loggerFlashSession = hc.createHubFlashSession(0xFFFFFFFF);
//            LoggerFlashSession loggerFlashSession = hc.createLoggerFlashSession(new Timestamp(0));
            DataPacket packet=null;
            int packetCount =0;
            while ((packet = loggerFlashSession.getPrevPacket())!=null){
//                lgr.info("next data");
            System.out.println(packet);
            packetCount++;
            if (packetCount==10000)
                break;
            }
System.out.println("koniec");

            
/*            
            LoggerFlashSessionDBInserter loggerFlashSessionDBInserter = LoggerFlashSessionDBInserter.createLoggerFlashSessionDBInserter(hc, null);
            loggerFlashSessionDBInserter.mainThread();
*/            
            
//            HubSessionManager.downloadMeasurmentsFromLogger(hub.getHubHexId(), null);
            
            }
            finally{
//                Thread.sleep(10000);
                if (hc!=null)
                    hc.close();
            }
            
            
        }
    
}
