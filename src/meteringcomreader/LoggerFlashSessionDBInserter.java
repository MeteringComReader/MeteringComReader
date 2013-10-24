/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import java.sql.SQLException;
import meteringcomreader.exceptions.MeteringSessionException;
import java.sql.Timestamp;
import java.util.HashSet;
import static meteringcomreader.SessionDBInserter.insertMeasurmentSQL;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Juliusz
 */
public class LoggerFlashSessionDBInserter extends SessionDBInserter{
    /**
     * Utworzenie loggera systemowego
     */
    private static final Logger lgr = LoggerFactory.getLogger(LoggerFlashSessionDBInserter.class);
    

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
    public int  mainThread() throws MeteringSessionException {
        DataPacket dp;
        int measurmentsCount=0;
//            if (!getLoggerId(this.metSess.)){  //nie ma loggera w bazie danych
//                conn.commit();
//                return ;
//            }
        try {
            while ((dp = metSess.getNextPacket(3))!=null) {
                        measurmentsCount+=loadPacket(dp);
                        lgr.debug("Time:"+System.nanoTime()+","+dp);
                }
           lgr.info("Time:"+System.nanoTime()+", new measurments inserted"+measurmentsCount);                        
        } finally {
            try {close();} catch (MeteringSessionException e) {/*ignore it*/}
        }
        return measurmentsCount;
    }
    
 }
