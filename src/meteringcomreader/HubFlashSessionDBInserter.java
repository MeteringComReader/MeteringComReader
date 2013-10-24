/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import meteringcomreader.exceptions.MeteringSessionException;
import java.sql.Timestamp;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Juliusz
 */
public class HubFlashSessionDBInserter extends SessionDBInserter{
    /**
     * Utworzenie loggera systemowego
     */
    private static final Logger lgr = LoggerFactory.getLogger(HubFlashSessionDBInserter.class);
    

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
    public int mainThread() throws MeteringSessionException {
        DataPacket dp;
        int measurmentsCount=0;
        try {
            while ((dp = metSess.getNextPacket(3))!=null) {
                        measurmentsCount+=loadPacket(dp);
                        lgr.info("Time:"+System.nanoTime()+","+dp);
                }
       lgr.info("Time:"+System.nanoTime()+", new measurments inserted"+measurmentsCount);            
        } finally {
            try {close();} catch (MeteringSessionException e) {/*ignore it*/}
        }
        return 0; //TODO: wyliczyć liczbę wierszy
    }
    
}
