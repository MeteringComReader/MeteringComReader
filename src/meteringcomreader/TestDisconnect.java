/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import meteringcomreader.exceptions.MeteringSessionException;
import gnu.io.SerialPortEvent;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Juliusz12
 */
public class TestDisconnect {
    public static void main(String[] args) throws MeteringSessionException{
        
        System.out.println("SerialPortEvent.DATA_AVAILABLE="+SerialPortEvent.DATA_AVAILABLE);
        System.out.println("SerialPortEvent.OUTPUT_BUFFER_EMPTY ="+SerialPortEvent.OUTPUT_BUFFER_EMPTY);                        
        
        HubConnection hc = null;
        try{
        Hub hub = new Hub(0, "COM34");
//        Hub hub = new Hub(0, "COM102");
         hc = HubConnection.createHubConnection(hub);
        long registerLogger = hc.registerLogger(0x5789de75);
        long[] registredLoggers = hc.getRegistredLoggers();
System.out.println("Regstred loggers count:"+registredLoggers.length);
for (int i=0; i<registredLoggers.length; i++){
System.out.println("Regstred logger:0x"+Long.toHexString(registredLoggers[i]));    
}
/*
        try{
System.out.println("closing session");             
        hc.sendCommand(Utils.closeHubFlashSessionReq);
        hc.receiveAck(Utils.closeHubFlashSessionRes);
System.out.println("closing session finished");             
         }
         catch(Exception e){
             System.out.println("first close error "+e.getMessage());
         }
        HubFlashSession s = hc.createHubFlashSession(new Timestamp(0));
            DataPacket dp;
            try {
                Thread.sleep(10000);
            } catch (InterruptedException ex) {
                lgr.warn(null, ex);
            }
        while((dp=s.getNextPacket())!=null){
            System.out.println(dp);
        }
        hc.closeHubFlashSession();
*/
        }
         finally{
           if (hc!=null){
             System.out.print("closing hc");
                               hc.close();
           }
         }
        

                /*        
                        try{
                            while (true){
                                hc.sendCommand(Utils.hubIdentifictionReq);
                                byte[] res = hc.receiveAck(Utils.hubIdentifictionAck);
                                System.out.println("Time:"+System.nanoTime()+","+"hub id is:"+Utils.bytes2long(res, 4));
                                    try {
                                        Thread.sleep(1000);
                                    } catch (InterruptedException ex) {
                                        lgr.warn(null, ex);
                                    }
                            }
                        }
                        finally{
                            if (hc!=null)
                                System.out.print("closing hc");
                               hc.close();
                        }
                        
                        
                  */;
        
    }
    
}
