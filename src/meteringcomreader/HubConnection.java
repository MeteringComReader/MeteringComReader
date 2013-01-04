/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import gnu.io.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Timestamp;
import java.util.Enumeration;
import java.util.TooManyListenersException;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author Juliusz
 */
public class HubConnection implements Runnable{

    OutputStream outputStream;
    InputStream inputStream;
    SerialPort serialPort = null;
    protected boolean canSendCommand =  true;
//    long hubId;
    Hub hub;
    ComReadDispatch crd;
    private RadioSession radioSession;
    private HubFlashSession hubFlashSession;
    private LoggerFlashSession loggerFlashSession;

    /*
     * protected static int protected static int protected static int
     */
    public static SerialPort initComPort(CommPortIdentifier portIdentifier) throws MeteringSessionException {
        SerialPort serialPort = null;
        try {
            serialPort = (SerialPort) portIdentifier.open("HubConnection", Utils.TIMEOUT);
            serialPort.setSerialPortParams(115200,
                    SerialPort.DATABITS_8,
                    SerialPort.STOPBITS_1,
                    SerialPort.PARITY_EVEN);
            serialPort.notifyOnOutputEmpty(true);
            serialPort.enableReceiveThreshold(1);
            serialPort.enableReceiveTimeout(Utils.TIMEOUT);
        } catch (UnsupportedCommOperationException ex) {
            throw new MeteringSessionException(ex);
        } catch (PortInUseException ex) {
            throw new MeteringSessionException(ex);
        }
        return serialPort;
    }
    protected boolean runHearbeat = true;
    protected Thread heartBeatThread = null;

    private  HubConnection() throws MeteringSessionException {
    }

    public static HubConnection createHubConnection(Hub hub) throws MeteringSessionException {
        HubConnection hc = new HubConnection();
        try {
            CommPortIdentifier portIdentifier = CommPortIdentifier.getPortIdentifier(hub.getComPortName());
            SerialPort serialPort = HubConnection.initComPort(portIdentifier);
            
            hc.setInputStream(serialPort.getInputStream());            
            hc.setOutputStream(serialPort.getOutputStream());
            hc.setSerialPort(serialPort);
            hc.setHub(hub);

            ComReadDispatch crd = new ComReadDispatch(hc.getInputStream());
            hc.setCrd(crd);
            serialPort.addEventListener(crd);
            serialPort.notifyOnDataAvailable(true); 
            
            hc.setHeartBeatThread(new Thread(hc, "HeartBeatThread for hub 0x"+hub.getHubHexId()));
//            hc.getHeartBeatThread().start(); //TODO: enable heartBeat

        } catch (TooManyListenersException ex) {
            throw new MeteringSessionException(ex);
        } catch (IOException ex) {
            throw new MeteringSessionException(ex);
        } catch (NoSuchPortException ex) {
            throw new MeteringSessionException(ex);
        }
        return hc;
    }

    public static Hubs discoverHubs(HubsSessions hs) {

        Hubs hubs = new Hubs(10);
        Enumeration portList = CommPortIdentifier.getPortIdentifiers();
        OutputStream outputStream = null;
        InputStream inputStream = null;
        long hubId;
        SerialPort serialPort = null;


        byte[] buf = new byte[4];

        while (portList.hasMoreElements()) {
            CommPortIdentifier portId = (CommPortIdentifier) portList.nextElement();

            if (portId.getPortType() == CommPortIdentifier.PORT_SERIAL) {
                String comPort = portId.getName();
System.out.println("Time:"+System.nanoTime()+","+"Found port " + comPort);
                if (hs.isPortUsed(comPort))
                    continue;
//TODO: usunąć!          
/*                
                if ("COM5".equals(portId.getName())) {
                    continue;
                }
*/

                try {
                    if (portId.isCurrentlyOwned()) {
                        throw new MeteringSessionException("Serial port currently in use");
                    }
                    serialPort = HubConnection.initComPort(portId);

                    outputStream = serialPort.getOutputStream();
                    inputStream = serialPort.getInputStream();
                    Utils.sendCommand(outputStream, Utils.closeAllSessionReq);
                    try {
                        Thread.sleep(Utils.TIMEOUT);
                    } catch (InterruptedException ex) {
                        //ignore
                    }

                    Utils.cleanInputStream(inputStream);

                    Utils.sendCommand(outputStream, Utils.hubIdentifictionReq);
                    if (inputStream.read() != (Utils.hubIdentifictionAck & 0x00FF)) {
                        continue;
                    }
                    if (inputStream.read() != ((Utils.hubIdentifictionAck >>> 8) & 0x00FF)) {
                        continue;
                    }
                    Utils.readBytes(inputStream, buf, 4);

                    hubId = Utils.bytes2long(buf, (byte) 4);
                    hubs.put(Hub.convertHubId2Hex(hubId), new Hub(hubId, comPort));
System.out.println("Time:"+System.nanoTime()+","+"hub found:" + portId.getName());
                } catch (MeteringSessionException ex) {

                    // Logger.getLogger(HubConnection.class.getName()).log(Level.SEVERE, null, ex);

                } catch (IOException e) {
System.out.println("Time:"+System.nanoTime()+","+e.getMessage());
                }
                finally {
                    HubConnection.closePort(inputStream, outputStream, serialPort);
                    serialPort = null;
                    inputStream = null;
                    outputStream = null;
                }
            }
        }
        return hubs;
    }

    static public void closePort(InputStream inputStream, OutputStream outputStream, SerialPort serialPort) {
        if (inputStream != null) {
            try {
                inputStream.close();
            } catch (IOException ex) {
                Logger.getLogger(HubConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

        try {
            if (outputStream != null) {
                outputStream.close();
            }
        } catch (IOException ex) {
            Logger.getLogger(HubConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        if (serialPort != null) {
            serialPort.close();
        }
    }

    public void close() {
        if (heartBeatThread!=null){
            setRunHearbeat(false);
            heartBeatThread.interrupt();
        }
        
        try {
            if (radioSession!=null)
                radioSession.close();
        } catch (MeteringSessionException ex) {
            Logger.getLogger(HubConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            if (this.hubFlashSession!=null) 
                hubFlashSession.close();
        } catch (MeteringSessionException ex) {
            Logger.getLogger(HubConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        try {
            if (this.loggerFlashSession!=null) 
                loggerFlashSession.close();
        } catch (MeteringSessionException ex) {
            Logger.getLogger(HubConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        serialPort.removeEventListener();
        HubConnection.closePort(inputStream, outputStream, serialPort);
        serialPort = null;
        inputStream = null;
        outputStream = null;
    }

    public HubFlashSession createHubFlashSession(Timestamp time) throws MeteringSessionException {
        hubFlashSession = new HubFlashSession(this, time);
        return hubFlashSession;
    }
    
    public void closeHubFlashSession() throws MeteringSessionException {
      if(hubFlashSession!=null)
        try{
            hubFlashSession.close();
        }
        finally{
            hubFlashSession = null;
        }
    }

    public RadioSession createRadioSession(int timeout) throws MeteringSessionException {
        radioSession = new RadioSession(this, timeout);
        return radioSession;
    }

    public boolean isHubPoweredAfterSession() throws MeteringSessionException {
        sendCommand(Utils.isHubPoweredAfterSessionReq);
        byte[] data = receiveAck(Utils.isHubPoweredAfterSessionRes);
        if (data[0] == 0x00) {
            return false;
        }
        return true;
    }

    public void setHubPoweredAfterSession(boolean shouldPowered) throws MeteringSessionException {
        if (shouldPowered) {
            sendCommand(Utils.setHubPoweredAfterSessionTrueReq);
            receiveAck(Utils.setHubPoweredAfterSessionTrueRes);
        } else {
            sendCommand(Utils.setHubPoweredAfterSessionFalseReq);
            receiveAck(Utils.setHubPoweredAfterSessionFalseRes);
        }
    }

    public void getPeriodIntervalHubFlashMemMode(Timestamp startInterval, Timestamp stopInterval) throws MeteringSessionException {
        long time;
        sendCommand(Utils.getPeriodIntervalHubFlashMemModeReq);
        byte[] data = receiveAck(Utils.getPeriodIntervalHubFlashMemModeRes);
        if (data != null) {
            time = Utils.bytes2long(data, 4);
            Utils.setTimestamp(startInterval, time);
            time = Utils.bytes2long(data, 3, 4);
            Utils.setTimestamp(stopInterval, time);
        }
    }

    public int getHubFlashMemMode() throws MeteringSessionException {
        sendCommand(Utils.getHubFlashMemModeReq);
        byte[] data = receiveAck(Utils.getHubFlashMemModeRes);
        return data[0];
    }

    public long[] getRegistredLoggers() throws MeteringSessionException {
        sendCommand(Utils.getRegistredLoggersReq);
        byte[] data = receiveAck(Utils.getRegistredLoggersRes);
        if (data==null)
            return new long[0];
        int loggersCount = data.length/4;
        long loggers[] = new long[loggersCount];
        for (int i = 0; i < loggersCount; i += 4) {
            loggers[i] = Utils.bytes2long(data, i, 4);
        }
        return loggers;
    }

    public void enableOverrideHubFlashMemMode() throws MeteringSessionException {
        sendCommand(Utils.enableOverwriteHubFlashMemModeReq);
        receiveAck(Utils.enableOverwriteHubFlashMemModeAck);
    }

    public void enableIntervalHubFlashMemMode(Timestamp start, Timestamp stop) throws MeteringSessionException {
        byte[] intervals = new byte[8];
        long startInt = Utils.timestamp2int(start);
        long stopInt = Utils.timestamp2int(stop);
        Utils.long2bytes(intervals, 0, startInt, 4);
        Utils.long2bytes(intervals, 4, stopInt, 4);
        sendCommand(Utils.enableIntervalHubFlashMemModeReq, intervals);
        receiveAck(Utils.enableIntervalHubFlashMemModeAck);
    }

    public void disableOverrideHubFlashMemMode() throws MeteringSessionException {
        sendCommand(Utils.disableOverwriteHubFlashMemModeReq);
        receiveAck(Utils.disableOverwriteHubFlashMemModeAck);
    }

    public void disableIntervalHubFlashMemMode() throws MeteringSessionException {
        byte[] intervals = new byte[8];

        sendCommand(Utils.disableIntervalHubFlashMemModeReq, intervals);
        receiveAck(Utils.disableIntervalHubFlashMemModeAck);
    }

    public long registerLogger(long loggerId) throws MeteringSessionException {
        sendCommand(Utils.registerLoggerReq, loggerId, (byte) 4);
        byte[] data = receiveAck(Utils.registerLoggerAck);
        return Utils.bytes2long(data, 4);
    }

    public long unregisterLogger(long loggerId) throws MeteringSessionException {
        sendCommand(Utils.unregisterLoggerReq, loggerId, (byte) 4);
        byte[] data = receiveAck(Utils.unregisterLoggerAck);
        return Utils.bytes2long(data, 4);
    }

    public int getChargeHubBatteryLevel() throws MeteringSessionException {
        sendCommand(Utils.getChargeHubBatteryLevelReq);
        byte[] data = receiveAck(Utils.getChargeHubBatteryLevelRes);
        return data[0];
    }

    public Timestamp getHubTime() throws MeteringSessionException {
        sendCommand(Utils.getHubTimeReq);
        byte[] data = receiveAck(Utils.getHubTimeReq);
        long timeInt = Utils.bytes2long(data, 4);
        Timestamp time = Utils.time2Timestamp(timeInt);
        return time;
    }

    public void setHubTime(Timestamp t) throws MeteringSessionException {
        long timeInt = Utils.timestamp2int(t);
        byte[] data = Utils.long2bytes(timeInt, 4);
        sendCommand(Utils.setHubTimeReq, data);
        receiveAck(Utils.setHubTimeReq);
    }

    //TODO: 0x02, 0x0A, 0x0B, 
    void sendCommand(int command) throws MeteringSessionException {
        sendCommand(command, null);
    }

    synchronized void sendCommand(int command, byte[] data) throws MeteringSessionException {
System.out.println("Time:"+System.nanoTime()+","+"wait sendCommand 0x"+String.format("%4x", command)+ 
        " thread: "+Thread.currentThread().getName());        
        while(!canSendCommand){
            try {
                wait(1000);
            } catch (InterruptedException e) {}
            if(!canSendCommand){
                throw new MeteringSessionException("Timeout during wait to send command:"+String.format("%4x", command));
            }
                
        }
System.out.println("Time:"+System.nanoTime()+","+"start sendCommand 0x"+String.format("%4x", command)+ 
        " thread: "+Thread.currentThread().getName());          
        canSendCommand = false;
        if (Thread.currentThread().isInterrupted()) 
           return;
        Utils.sendCommand(outputStream, command, data);
    }

    void sendCommand(int command, long parameter, byte size) throws MeteringSessionException {
        sendCommand(command);
        byte buf[] = Utils.long2bytes(parameter, size);

        try {
            outputStream.write(buf);
            outputStream.flush();
        } catch (IOException ex) {
            throw new MeteringSessionException(ex);
        }
    }

    int readBytes(byte[] buf, int size) throws MeteringSessionException {
        return Utils.readBytes(inputStream, buf, size);
    }

    public void cleanInputStream() throws MeteringSessionSPException {
        Utils.cleanInputStream(inputStream);
    }

    synchronized byte[] receiveAck(int ack) throws MeteringSessionException {
        byte[] ret = null;
        try{
            ComResp rs = crd.getNextResp();
            rs.receiveAck(ack);
            ret = rs.receiveData();
        }
        finally{
            canSendCommand=true;
            notifyAll();
        }
System.out.println("Time:"+System.nanoTime()+","+"done receiveAck 0x"+String.format("%4x", ack)+ 
        " thread: "+Thread.currentThread().getName());         
        return ret;

    }

    /**
     * @return the outputStream
     */
    public OutputStream getOutputStream() {
        return outputStream;
    }

    /**
     * @param outputStream the outputStream to set
     */
    public void setOutputStream(OutputStream outputStream) {
        this.outputStream = outputStream;
    }

    /**
     * @return the inputStream
     */
    public InputStream getInputStream() {
        return inputStream;
    }

    /**
     * @param inputStream the inputStream to set
     */
    public void setInputStream(InputStream inputStream) {
        this.inputStream = inputStream;
    }

    /**
     * @return the serialPort
     */
    public SerialPort getSerialPort() {
        return serialPort;
    }

    /**
     * @param serialPort the serialPort to set
     */
    public void setSerialPort(SerialPort serialPort) {
        this.serialPort = serialPort;
    }

    /**
     * @return the hub
     */
    public Hub getHub() {
        return hub;
    }

    /**
     * @param hub the hub to set
     */
    public void setHub(Hub hub) {
        this.hub = hub;
    }

    /**
     * @return the crd
     */
    public ComReadDispatch getCrd() {
        return crd;
    }

    /**
     * @param crd the crd to set
     */
    public void setCrd(ComReadDispatch crd) {
        this.crd = crd;
    }

    public RadioSession getRadioSession() {
        return radioSession;
    }

/*    String getHubHexId() {
        throw new UnsupportedOperationException("Not yet implemented");
    }
    * 
    */

    @Override
    public void run() {
        
        try{
            while (isRunHearbeat()){
                try {
                        Thread.sleep(1000*9);
                }
                catch (InterruptedException ex) {
                        //ignore
                } 
                if (!Thread.interrupted()){
                    sendCommand(Utils.hubIdentifictionReq);
//                try{
                    byte[] res = receiveAck(Utils.hubIdentifictionAck);
System.out.println("Time:"+System.nanoTime()+","+Utils.bytes2long(res, 4));
//                }
//                catch (MeteringSessionTimeoutException e){
                    //ignore it
//                }
                }
               
            }
        }
     catch (MeteringSessionException ex){
System.out.println("Time:"+System.nanoTime()+","+"Thread:"+Thread.currentThread().getName()+ex);         
//Logger.getLogger(HubConnection.class.getName()).log(Level.SEVERE, null, ex);
               heartBeatThread=null;
               HubSessionManager.closeHubSession(hub.getHubHexId());
     }
     finally{
System.out.println("Time:"+System.nanoTime()+","+"Thread:"+Thread.currentThread().getName()+" stopped.");            
        }
        
    }

    /**
     * @return the runHearbeat
     */
    synchronized public boolean isRunHearbeat() {
        return runHearbeat;
    }

    /**
     * @param runHearbeat the runHearbeat to set
     */
    synchronized public void setRunHearbeat(boolean runHearbeat) {
        this.runHearbeat = runHearbeat;
    }

    /**
     * @return the heartBeatThread
     */
    public Thread getHeartBeatThread() {
        return heartBeatThread;
    }

    /**
     * @param heartBeatThread the heartBeatThread to set
     */
    public void setHeartBeatThread(Thread heartBeatThread) {
        this.heartBeatThread = heartBeatThread;
    }

    public MeteringSession createLoggerFlashSession(Timestamp start) throws MeteringSessionException {
        loggerFlashSession = new LoggerFlashSession(this, start);
        return loggerFlashSession;    
    }
    
    public void closeLoggerFlashSession() throws MeteringSessionException {
      if(loggerFlashSession!=null)
        try{
            loggerFlashSession.close();
        }
        finally{
            loggerFlashSession = null;
        }
    }
    
    public long getLoggerId() throws MeteringSessionException{
        sendCommand(Utils.getLoggerIdReq);
        byte[] data = receiveAck(Utils.getLoggerIdReq);
        return Utils.bytes2long(data, 4);
    }
    
    
    public void enableLoggerRadio(long loggerId) throws MeteringSessionException {
        long currentId=getLoggerId();
        if (currentId!=loggerId)
            throw new MeteringSessionException("Incorrect logger identification number");
        sendCommand(Utils.enableLoggerRadioReq);
        receiveAck(Utils.enableLoggerRadioAck);           
    }
}
