/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import gnu.io.CommPortIdentifier;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.TimeZone;

/**
 *
 * @author Juliusz
 */
public class Utils {
    
    final static int startHubFlashSessionReq = 0x0202;
    final static int startHubFlashSessionRes=0x0202;
    
    final static int hubIdentifictionReq=0x0001;
    final static int isHubPoweredAfterSessionReq=0x0003;
    
    final static int setHubPoweredAfterSessionTrueReq=0x0104;
    final static int setHubPoweredAfterSessionFalseReq=0x0804;
    
    final static int getHubFlashMemModeReq=0x0005;
        
    final static int enableIntervalHubFlashMemModeReq=0x0205;
    final static int disableIntervalHubFlashMemModeReq=0x0A05;
    final static int enableOverwriteHubFlashMemModeReq=0x0105;
    final static int disableOverwriteHubFlashMemModeReq=0x0905;
    
    final static int enableIntervalHubFlashMemModeAck=0x0205;
    final static int disableIntervalHubFlashMemModeAck=0x0A05;
    final static int enableOverwriteHubFlashMemModeAck=0x0105;
    final static int disableOverwriteHubFlashMemModeAck=0x0905;
    
    final static int getPeriodIntervalHubFlashMemModeReq=0x0305;
    final static int getPeriodIntervalHubFlashMemModeRes=0x0305;

    final static int getRegistredLoggersReq=0x0006;
    final static int unregisterLoggerReq=0x0106;
    final static int registerLoggerReq=0x0206;
    final static int getChargeHubBatteryLevelReq=0x0007;
    final static int closeAllSessionReq=0xFF02;
    final static int isHubPoweredAfterSessionRes=0x0003;
    

    final static int hubIdentifictionAck=0xAA01;
    final static int setHubPoweredAfterSessionTrueRes=0x0804;
    final static int setHubPoweredAfterSessionFalseRes=0x0004; 
    
    final static int getHubFlashMemModeRes=0x0005;
    
    final static int getRegistredLoggersRes=0x0006;
    final static int registerLoggerAck=0x0206;
    final static int unregisterLoggerAck=0x0106;
    
    final static int radioSessionRes = 0x000A;
    final static int startRadioSessionReq = 0x0002;
    final static int startRadioSessionRes = 0x0002;
    final static int closeRadioSessionReq=0xF002;
    final static int closeRadioSessionRes=0x0802;
    
//    static final int TIMEOUT=1000000;
    static final int TIMEOUT=100;

    static final int intervalHubFlashMemMode=1;
    static final int overwriteHubFlashMemMode=2;
            
    public static  String timeStartPointStr = "1970-01-01 00:00:00";
    public static long timeStartPoint = Timestamp.valueOf(Utils.timeStartPointStr).getTime()/1000; //in seconds
    public static Calendar UTCcalendar=Calendar.getInstance(TimeZone.getTimeZone("GMT+00:00")) ;
    
    final static int startLoggerFlashRes=0x0102;
    static final int startLoggerFlashSessionReq=0x0102;

    final static int closeAllSessionRes=0xFF02;
    final static int closeLoggerFlashSessionRes=0x0902;
    final static int closeLoggerFlashSessionReq=0x0902;
    final static int getloggersRes=0x0006;
    final static int getChargeHubBatteryLevelRes=0x0007;
    final static int getHubTimeReq=0x0107;
    final static int getHubTimeRes=0x0107;
    final static int setHubTimeReq=0x0207;
    final static int setHubTimeAck=0x0207;
    final static int getLoggerTimeRes=0x0008;
    final static int getFreqLoggingRes=0x000C;
    final static int getLoggerIdReq=0x000D;
    final static int getLoggerIdRes=0x000D;
    final static int enableLoggerRadioReq=0x010D;
    final static int enableLoggerRadioAck=0x010D;
    
    final static int getNextHubFlashSessionReq=0x020A;
    final static int getNextHubFlashSessionRes=0x020A;
    final static int closeHubFlashSessionReq=0xF202;
    final static int closeHubFlashSessionRes=0x0A02;
    
    static final int getNextLoggerFlashSessionReq=0x010A;
    static final int getNextLoggerFlashSessionRes=0x010A;
    
    static byte[] long2bytes(long data, int i) {
        byte[] ret= new byte[i];
        for(byte u=0; u<i; u++)
            ret[u]=(byte)((data>>>(u*8))&0x000000FFL);
        return ret;
    }
    
    static void long2bytes(byte[] output, int start, long input, int size){
        for (int i=start; i<start+size; i++){
            output[i]=(byte)((input>>>(8*i))&0xFF);
        }        
    }
    
    static long bytes2long(byte[] data, int start, int size){
        long ret=0;
        for(int u=(size-1)+start; u>=start; u--)
            ret=(ret<<8)| (((long)data[u])&0xFFL);
        return ret;
    }
    static long bytes2long(byte[] data, int size){
        return bytes2long(data, 0, size);
    }
    
        static String getPortTypeName ( int portType )
    {
        switch ( portType )
        {
            case CommPortIdentifier.PORT_I2C:
                return "I2C";
            case CommPortIdentifier.PORT_PARALLEL:
                return "Parallel";
            case CommPortIdentifier.PORT_RAW:
                return "Raw";
            case CommPortIdentifier.PORT_RS485:
                return "RS485";
            case CommPortIdentifier.PORT_SERIAL:
                return "Serial";
            default:
                return "unknown type";
        }
    }
    static long timestamp2int(Timestamp t){
        return t.getTime()/1000 - timeStartPoint;
    }
    
    static void setTimestamp(Timestamp t, long secondsTime){
        t.setTime((timeStartPoint+secondsTime)*1000);
    }
    
    static Timestamp time2Timestamp(long secondsTime){
        Timestamp t = new Timestamp(0);
        setTimestamp(t, secondsTime);
        return t;
    }

    static void sendCommand(OutputStream outputStream, int command) throws MeteringSessionException {
        Utils.sendCommand(outputStream, command, null);
    }

    static void sendCommand(OutputStream outputStream, int command, byte[] data) throws MeteringSessionException {
        byte buf[]=Utils.long2bytes(command, (byte)0x02);
        
        try {
            outputStream.write(buf);
            if (data!=null) 
                outputStream.write(data);
            outputStream.flush();
        } catch (IOException ex) {
            throw new MeteringSessionSPException(ex);
        }    
    }

    static void cleanInputStream(InputStream inputStream) throws MeteringSessionSPException {
        try {
            while (inputStream.read() > 0);
        } catch (IOException ex) {
            throw new MeteringSessionSPException(ex);
        }
    }

    static int readBytes(InputStream inputStream, byte[] buf, int size) throws MeteringSessionException {
        byte ret[] = new byte[1];
        int len;


        for (int i = 0; i < size; i++) {
            try {
                len = inputStream.read(ret);
                if (len == -1) {
                    throw new MeteringSessionException("Serial EOF");
                }
                if (len == 0) {
                    throw new MeteringSessionTimeoutException("Serial port read timeout");
                }
                buf[i] = ret[0];
            } catch (IOException ex) {
                throw new MeteringSessionSPException(ex);
            }
        }
        return size;    
    }

    public static long hexId2long(String measurerNo) {
        String idWithoutPrefix=measurerNo.substring(4, measurerNo.length());
        long id = Long.parseLong(idWithoutPrefix, 16);
        return id;
    }

}
