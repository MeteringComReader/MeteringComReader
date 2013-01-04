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
public class HubFlashSessionDBInserter extends SessionDBInserter{

    Timestamp start;
    public HubFlashSessionDBInserter(HubConnection hc, Timestamp start) throws MeteringSessionException {
        super(hc);
        this.start=start;
    }

    static public HubFlashSessionDBInserter createHubFlashSessionDBInserter(HubConnection hc, Timestamp start) throws MeteringSessionException {
        HubFlashSessionDBInserter sessDBInsert = new HubFlashSessionDBInserter(hc, start);
        sessDBInsert.metSess = hc.createHubFlashSession(start);
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
