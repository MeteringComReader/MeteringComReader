/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Juliusz
 */
public class RadioSessionDBInserter extends SessionDBInserter implements Runnable{
    /**
     * Utworzenie loggera systemowego
     */
    private static final Logger lgr = LoggerFactory.getLogger(RadioSessionDBInserter.class);
    
     protected boolean  shouldRun=true;
     protected Thread thread=null;


    public RadioSessionDBInserter(HubConnection hc) throws MeteringSessionException {
        super(hc);
    }
    
    
    static public RadioSessionDBInserter createRadioSessionDBInserter(HubConnection hc) throws MeteringSessionException {
        RadioSessionDBInserter mcr = new RadioSessionDBInserter(hc);
        mcr.metSess = mcr.hc.getRadioSession();

        return mcr;
    }
    @Override
     protected void upsertLoggerStatus(DataPacket dp) throws MeteringSessionException {
         if (!insertLoggerStatus(dp))
             updateLoggerStatus(dp);
     }

    @Override
    public void mainThread() throws MeteringSessionException {
        setThread(new Thread(this, "radioSessionDBInserter for hub: "+hc.hub.getHubHexId()));
        getThread().start();
    }

    
    @Override
    public void run() {
        DataPacket dp;
        try {
            while (isShouldRun()) {
                dp = metSess.getNextPacket();
                loadPacket(dp);
lgr.debug("Time:"+System.nanoTime()+","+dp);
            } 
        }catch (MeteringSessionException tout) {
            
        }
        finally{
            setThread(null);
            //TODO: close remaining resoureses
        }
lgr.debug("Time:"+System.nanoTime()+","+"Thread stoped: "+Thread.currentThread().getName());
    }
    
    @Override
    public void close() throws MeteringSessionException{
        setShouldRun(false);
        if (getThread()!=null)
            getThread().interrupt();
        super.close();
    }

    /**
     * @return the shouldRun
     */
    synchronized protected boolean isShouldRun() {
        return shouldRun;
    }

    /**
     * @param shouldRun the shouldRun to set
     */
    synchronized protected void setShouldRun(boolean shouldRun) {
        this.shouldRun = shouldRun;
    }

    /**
     * @return the thread
     */
    synchronized public Thread getThread() {
        return thread;
    }

    /**
     * @param thread the thread to set
     */
    synchronized public void setThread(Thread thread) {
        this.thread = thread;
    }

}
