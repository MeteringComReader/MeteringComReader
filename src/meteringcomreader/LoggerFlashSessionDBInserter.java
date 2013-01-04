/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import java.sql.Timestamp;

/**
 *
 * @author Juliusz
 */
public class LoggerFlashSessionDBInserter extends SessionDBInserter{

    Timestamp start;
    public LoggerFlashSessionDBInserter(HubConnection hc, Timestamp start) throws MeteringSessionException {
        super(hc);
        this.start=start;
    }

    static public LoggerFlashSessionDBInserter createLoggerFlashSessionDBInserter(HubConnection hc, Timestamp start) throws MeteringSessionException {
        LoggerFlashSessionDBInserter sessDBInsert = new LoggerFlashSessionDBInserter(hc, start);
        sessDBInsert.metSess = hc.createLoggerFlashSession(start);
        return sessDBInsert;
    }
        
    @Override
    protected void upsertLoggerStatus(DataPacket dp) throws MeteringSessionException {
        ; //do not insert logger info
    }

    @Override
    public void mainThread() throws MeteringSessionException {
        DataPacket dp;
        try {
            while ((dp = metSess.getNextPacket())!=null) {
                        loadPacket(dp);
System.out.println("Time:"+System.nanoTime()+","+dp);
                }
        } finally {
            try {close();} catch (MeteringSessionException e) {/*ignore it*/}
        }
    }
    
}
