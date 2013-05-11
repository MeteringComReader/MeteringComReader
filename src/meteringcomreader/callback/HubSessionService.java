/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader.callback;



import java.util.Date;
import java.sql.Timestamp;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import meteringcomreader.*;

/**
 *
 * @author Juliusz
 */
public class HubSessionService {


    public void downloadMeasurmentsFromHub(String hubNo, Date from) throws MeteringSessionException {
        try{
        Timestamp time = new Timestamp(from.getTime());
        HubSessionManager.downloadMeasurmentsFromHub(hubNo, time);
        HubConnection hc = HubSessionManager.getHubsSessions().getHubConnection(hubNo);
        HubFlashSessionDBInserter inserter= HubFlashSessionDBInserter.createHubFlashSessionDBInserter(hc, time);
        inserter.mainThread();
        }catch (Throwable ex){
            ex.printStackTrace();
            throw new MeteringSessionException(ex.getMessage());
        }
        
    }

    public void downloadMeasurmentsFromLogger(String hubNo, Date from) throws MeteringSessionException {
        try{
        Timestamp time = new Timestamp(from.getTime());
        HubSessionManager.downloadMeasurmentsFromLogger(hubNo, time);
        HubConnection hc = HubSessionManager.getHubsSessions().getHubConnection(hubNo);
        LoggerFlashSessionDBInserter inserter= LoggerFlashSessionDBInserter.createLoggerFlashSessionDBInserter(hc, time);
        inserter.mainThread();
        }catch (Throwable ex){
            ex.printStackTrace();
            throw new MeteringSessionException(ex.getMessage());
        }
        
    }

        static private void mergeStackTraces(Throwable error, int currentStackLimit)
    {
        StackTraceElement[] currentStack =
            new Throwable().getStackTrace();
        currentStackLimit = 1; // TODO: raussuchen
                StackTraceElement[] oldStack =
            error.getStackTrace();
        StackTraceElement[] zusammen =
            new StackTraceElement[currentStack.length - currentStackLimit +
                                  oldStack.length + 1];
        System.arraycopy(oldStack, 0, zusammen, 0, oldStack.length);
        zusammen[oldStack.length] =
            new StackTraceElement("===",
                                  "<->",
                                  "", -3);
        System.arraycopy(currentStack, currentStackLimit,
                         zusammen, oldStack.length+1,
                         currentStack.length - currentStackLimit);
        error.setStackTrace(zusammen);
    }

    public void intervalHubFlashMemoryMode(String hubNo, 
            Date startTime, Date endTime, 
            boolean enable) throws MeteringSessionException {
        try{
        HubConnection hc = HubSessionManager.getHubsSessions().getHubConnection(hubNo);
        if (enable){
           Timestamp startTimestamp = new Timestamp(startTime.getTime());
           Timestamp endTimestamp = new Timestamp(endTime.getTime());
           hc.enableIntervalHubFlashMemMode(startTimestamp, endTimestamp);
        }
        else
            hc.disableIntervalHubFlashMemMode();
        }catch (Throwable ex){
            ex.printStackTrace();
            throw new MeteringSessionException(ex.getMessage());
        }

    }

    public void overwriteHubFlashMemoryMode(String hubNo, boolean enable) throws MeteringSessionException  {
        try{
        HubConnection hc = HubSessionManager.getHubsSessions().getHubConnection(hubNo);
        if (enable)
            hc.enableOverrideHubFlashMemMode();
        else
            hc.disableOverrideHubFlashMemMode();
        }catch (Throwable ex){
            ex.printStackTrace();
            throw new MeteringSessionException(ex.getMessage());
        }

    }

    public void registerMeasurer(String hubNo, String measurerNo)  throws MeteringSessionException {
        try{
        HubConnection hc = HubSessionManager.getHubsSessions().getHubConnection(hubNo);
        hc.registerLogger(Utils.hexId2long(measurerNo));
        }catch (Throwable ex){
            ex.printStackTrace();
            throw new MeteringSessionException(ex.getMessage());
        }
        
    }

    public void unregisterMeasurer(String hubNo, String measurerNo)  throws MeteringSessionException {
        try{
        HubConnection hc = HubSessionManager.getHubsSessions().getHubConnection(hubNo);
        hc.unregisterLogger(Utils.hexId2long(measurerNo));
        }catch (Throwable ex){
            ex.printStackTrace();
            throw new MeteringSessionException(ex.getMessage());
        }
        
    }

    public void measurerRadio(String hubNo, String measurerNo,  boolean enable)  throws MeteringSessionException {
        try{
        HubConnection hc = HubSessionManager.getHubsSessions().getHubConnection(hubNo);
        if (enable){
            hc.enableLoggerRadio(Utils.hexId2long(measurerNo));
        }           
        else
            ;
        }catch (Throwable ex){
            ex.printStackTrace();
            throw new MeteringSessionException(ex.getMessage());
        }
        
    }

}
