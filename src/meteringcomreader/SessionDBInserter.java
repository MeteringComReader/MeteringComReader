package meteringcomreader;

import java.math.BigDecimal;
import java.sql.*;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Juliusz
 */
abstract public class SessionDBInserter {
    /**
     * Utworzenie loggera systemowego
     */
    private static final Logger lgr = LoggerFactory.getLogger(SessionDBInserter.class);

    
    protected static String checkPeriodSQL = "select MT_TIME from measurment where mt_ms_id=? and MT_TIME between ? and ?";
    protected static String insertMeasurmentSQL = "insert into measurment (MT_ID, MT_MS_ID, MT_PA_ID, MT_TIME, MT_MECH_VALUE1, MT_BATTERY_VOLTAGE) values (?,?,?,?,?,?)";
    protected static String getNextMeIdSQL = "select me_me_id_seq.nextval from dual";
    protected static String updateNewestMeasurmentSQL = "update measurer set MS_LAST_MT_ID=?, MS_BEFORE_LAST_MT_ID=MS_LAST_MT_ID where MS_ID=?";
    private String getLoggerIdSQL="select MS_ID from measurer where MS_NUMBER=? for update";
    protected static String insertConnectedLoggersSQL="insert into measurer_session (MSS_NUMBER,MSS_RSSI,MSS_HS_NUMBER,MSS_LAST_SEEN, mss_measurment_period) values (?,?,?,?,?)";
    protected static String updateConnectedLoggersSQL="update measurer_session set MSS_RSSI=?,  MSS_LAST_SEEN=?, mss_measurment_period=? where MSS_NUMBER=? and MSS_HS_NUMBER=?";    
    
    protected HubConnection hc = null;
    protected MeteringSession metSess = null;
    protected Connection conn;
    private PreparedStatement getNextMeIdPS=null;
    private PreparedStatement updateNewestMeasurmentPS=null;
    private PreparedStatement insertMeasurmentPS=null;
    private PreparedStatement getLoggerIdPS=null;
    private PreparedStatement checkPeriodPS=null;
    private PreparedStatement insertConnectedLoggersPS=null;
    private PreparedStatement updateConnectedLoggersPS=null;
    
    

    
    protected boolean insertLoggerStatus(DataPacket dp) throws MeteringSessionException {
        boolean ret;
        try {
            if (insertConnectedLoggersPS==null)
                insertConnectedLoggersPS=conn.prepareStatement(insertConnectedLoggersSQL);
           insertConnectedLoggersPS.setString(1, dp.getLoggerHexId());
           insertConnectedLoggersPS.setLong(2, dp.getRSSI());
           insertConnectedLoggersPS.setString(3, hc.hub.getHubHexId());
           java.util.Date date= new java.util.Date();
           insertConnectedLoggersPS.setTimestamp(4, new Timestamp(date.getTime()), Utils.UTCcalendar);
           insertConnectedLoggersPS.setInt(5, dp.measurmentPeriod);
           insertConnectedLoggersPS.execute();
           ret=true;
        } catch (SQLException ex) {
            int errCode=ex.getErrorCode();
            if (errCode==1){ //ORA-00001: unique constraint (METER.MEASUSER_SESSION_PK) violated
                ret=false;
            }
            else {
                throw new MeteringSessionException(ex);
            }    
        }
        return ret;        
    }

    abstract protected void upsertLoggerStatus(DataPacket dp) throws MeteringSessionException;

    protected void updateLoggerStatus(DataPacket dp) throws MeteringSessionException {
        try{
            if (updateConnectedLoggersPS==null)
                updateConnectedLoggersPS=conn.prepareStatement(updateConnectedLoggersSQL);
            updateConnectedLoggersPS.setLong(1, dp.getRSSI());
            java.util.Date date= new java.util.Date();
           updateConnectedLoggersPS.setTimestamp(2, new Timestamp(date.getTime()), Utils.UTCcalendar);
           updateConnectedLoggersPS.setInt(3, dp.measurmentPeriod);
           updateConnectedLoggersPS.setString(4, dp.getLoggerHexId());
           updateConnectedLoggersPS.setString(5, hc.hub.getHubHexId());
           updateConnectedLoggersPS.execute();
        }
        catch (SQLException ex) {
            throw new MeteringSessionException(ex);
        }    
    }


    protected void loadPacket(DataPacket dp) throws MeteringSessionException {
        boolean newMeasurments = false;
        long newestMeId = 0;

        try {
            if (insertMeasurmentPS==null)
                insertMeasurmentPS = conn.prepareStatement(insertMeasurmentSQL);
            upsertLoggerStatus(dp);
            if (!getLoggerId(dp)){
                conn.commit();
                return;
            }
/*            
            ResultSet timzoners = conn.createStatement().executeQuery("select sessiontimezone from dual");
            timzoners.next();
            String sessiontimezone = timzoners.getString(1);
            * 
            */
            HashSet<Timestamp> hs = checkPeriod(
                dp.loggerId,
                dp.measurmentTimeStart,
                dp.measurmentTimeEnd);
            for (int i = 0; i < dp.temperatures.length; i++) {
                Timestamp time = new Timestamp((Utils.timeStartPoint
                        + dp.endTime - i * dp.measurmentPeriod) * 1000);
                if (!hs.contains(time)) {
                    long meId = getNextMeId();
                    if (!newMeasurments){
                        newestMeId=meId;
                        newMeasurments = true;
                    }
                    addToBatch(insertMeasurmentPS, dp, meId, time, dp.temperatures[i]);
                }
            }
            if (newMeasurments) {               
                insertMeasurmentPS.executeBatch();
                updateNewestMeasurment(dp, newestMeId);
            }
            conn.commit();
        } catch (SQLException ex) {
            try {
                conn.rollback();
            } catch (SQLException ex1) {
                lgr.warn(null, ex1);
            }
            //throw new MeteringSessionException(ex);
lgr.debug("Time:"+System.nanoTime()+","+ex.getMessage());
        } 

    }

    protected HashSet<Timestamp> checkPeriod(long msId, Timestamp fromTime, Timestamp toTime) throws MeteringSessionException {

        HashSet<Timestamp> hs = null;
        try {
            
            hs = new HashSet<Timestamp>();
            if (checkPeriodPS==null){
                checkPeriodPS = conn.prepareStatement(checkPeriodSQL);
            }
            checkPeriodPS.setLong(1, msId);
            checkPeriodPS.setTimestamp(2, fromTime, Utils.UTCcalendar);
            checkPeriodPS.setTimestamp(3, toTime, Utils.UTCcalendar);
            ResultSet rs = checkPeriodPS.executeQuery();
            while (rs.next()) {
                hs.add(rs.getTimestamp(1, Utils.UTCcalendar));
            }
            rs.close();
        } catch (SQLException ex) {
            throw new MeteringSessionException(ex);
        }
        return hs;

    }

    protected SessionDBInserter(HubConnection hc) throws MeteringSessionException {
        super();
        this.hc=hc;
        conn=DBUtils.createDBConnection();
    }

    public void close() throws MeteringSessionException {
        if (conn != null) {
            try {
                conn.close();
            } catch (SQLException ex) {
                //ignore
            }
        }
    }

    abstract public void mainThread() throws MeteringSessionException;

/*    
    public static void main(String[] args) throws MeteringSessionException {
        
        Hubs hubs = HubConnection.discoverHubs();
        if (hubs.isEmpty()){
            lgr.info("no hubs found");
            return;
        }
        Iterator it =hubs.entrySet().iterator();
        if (it.hasNext()) {
             Map.Entry<String, Hub> pairs = (Map.Entry<String, Hub>)it.next();
            Hub hub = pairs.getValue();
            HubConnection hc = HubConnection.createHubConnection(hub);
            SessionDBInserter mcr = createRadioSessionDBInserter(hc);
            mcr.mainThread();
        }

    }
*/    
//MT_ID, MT_MS_ID, MT_PA_ID, MT_TIME, MT_MECH_VALUE1, MT_BATTERY_VOLTAGE
    private void addToBatch(PreparedStatement ps, DataPacket dp, long meId, Timestamp time, int temperature) throws SQLException {
        ps.setLong(1, meId); //MT_ID, 
        ps.setLong(2, dp.loggerId); //MT_MS_ID, 
        ps.setLong(3, 0); //MT_PA_ID, //TODO: dodac obsluge paczek
        ps.setTimestamp(4, time, Utils.UTCcalendar);        //MT_TIME, 
        ps.setBigDecimal(5, (new BigDecimal(temperature)).divide(BigDecimal.TEN));// MT_MECH_VALUE1
        ps.setBigDecimal(6, (new BigDecimal(dp.batteryVoltage)).divide(BigDecimal.TEN));
        ps.addBatch();

    }

    private long getNextMeId() throws SQLException {
        if (getNextMeIdPS==null){
            getNextMeIdPS=conn.prepareStatement(getNextMeIdSQL);
        }
        ResultSet rs= getNextMeIdPS.executeQuery();
        rs.next();
        long ret = rs.getLong(1);
        rs.close();
        return ret;
    }

    private void updateNewestMeasurment(DataPacket dp, long newestMeId) throws SQLException {
        if (updateNewestMeasurmentPS==null){
            updateNewestMeasurmentPS=conn.prepareStatement(updateNewestMeasurmentSQL);
        }
        updateNewestMeasurmentPS.setLong(1, newestMeId);
        updateNewestMeasurmentPS.setLong(2, dp.loggerId);        
        updateNewestMeasurmentPS.execute();
    }

    private boolean getLoggerId(DataPacket dp) throws SQLException {
        boolean ret=true;
        if (getLoggerIdPS==null){
            getLoggerIdPS=conn.prepareStatement(getLoggerIdSQL);
        }
        String hexLoggerNo=dp.getLoggerHexId();
        getLoggerIdPS.setString(1, hexLoggerNo);
        ResultSet rs= getLoggerIdPS.executeQuery();
        if (!rs.next())  //lgr.debug("Logger number:"+dp.loggerNo+" no found");
                return false;
        dp.loggerId= rs.getLong(1);
        rs.close();
        return ret;
    }
}
