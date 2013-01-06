package meteringcomreader;

import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * 
 * @author Juliusz Jezierski
 */
public class RadioSession extends MeteringSession implements Runnable{
    

    
//    protected static int startRadioSessionRes = 0x0002;
    protected int timeout;
    protected boolean run=true;
    protected Thread pinger = null;
    
    public RadioSession(HubConnection hc, int timeout) throws MeteringSessionException{
            super(hc);
            this.timeout=timeout;
            startRadioSession();
            pinger=new Thread(this,"RadioSessionPinger for hub:"+hc.hub.getHubHexId());
            pinger.start();
            
    }
    
    protected void startRadioSession() throws MeteringSessionException{
            hc.sendCommand(Utils.startRadioSessionReq, (long) timeout, (byte)1);
            hc.receiveAck(Utils.startRadioSessionRes);    
    }
    

    @Override
    public DataPacket getNextPacket() throws MeteringSessionException{
        return hc.crd.getNextRSPacket();
       
    }

    @Override
    public DataPacket regetPrevPacket() {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    @Override
    public void close() throws MeteringSessionException{
        setRun(false);
        if (getPinger()!=null)
            getPinger().interrupt();
        hc.sendCommand(Utils.closeRadioSessionReq);
        try{
            hc.receiveAck(Utils.closeRadioSessionRes);
        }
        catch (MeteringSessionTimeoutException ex){
            //ignore it
        }
    }

    @Override
    public void run() {
        while(isRun()){
            try {
                Thread.sleep(timeout*1000*60-1000*25);
            } catch (InterruptedException ex) {
                //ignore ir
            }
            if (isRun()){
                try {
                    startRadioSession();
                } catch (MeteringSessionException ex) {
                    Logger.getLogger(RadioSession.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }
        
        
        System.out.println("Time:"+System.nanoTime()+","+"Thread stoped: "+Thread.currentThread().getName());
    }

    /**
     * @return the pinger
     */
    public Thread getPinger() {
        return pinger;
    }

    /**
     * @param pinger the pinger to set
     */
    public void setPinger(Thread pinger) {
        this.pinger = pinger;
    }

    /**
     * @return the run
     */
    synchronized public boolean isRun() {
        return run;
    }

    /**
     * @param run the run to set
     */
    synchronized public void setRun(boolean run) {
        this.run = run;
    }
}
