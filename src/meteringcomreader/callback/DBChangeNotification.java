/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader.callback;

/**
 *
 * @author Juliusz
 */
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import meteringcomreader.ComReadDispatch;
import meteringcomreader.DBUtils;
import meteringcomreader.Hub;
import meteringcomreader.HubRequest;
import meteringcomreader.HubResponse;
import meteringcomreader.HubSessionManager;
import meteringcomreader.MeteringSessionException;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleStatement;
import oracle.jdbc.dcn.DatabaseChangeEvent;
import oracle.jdbc.dcn.DatabaseChangeListener;
import oracle.jdbc.dcn.DatabaseChangeRegistration;
import oracle.jdbc.dcn.QueryChangeDescription;
import oracle.jdbc.dcn.RowChangeDescription;
import oracle.jdbc.dcn.TableChangeDescription;
import oracle.sql.ROWID;
import org.apache.log4j.PropertyConfigurator;
import org.slf4j.LoggerFactory;
 
public class DBChangeNotification implements DatabaseChangeListener
{
      /**
     * Utworzenie loggera systemowego
     */
    private static final org.slf4j.Logger lgr = LoggerFactory.getLogger(ComReadDispatch.class);
    
    private static final String dbcQuery="select HR_HS_NUMBER from hub_requests where HR_HS_NUMBER=?";
    private static final String getHubReqests
            ="select HR_P1,HR_P2,HR_P3,HR_P4,HR_P5,HR_P6,HR_P7,HR_P8,HR_P9,HR_P10, HR_HS_NUMBER,HR_COMMAND from hub_requests where rowid=?";
    private static  PreparedStatement getHubReqestsPS=null;
    private static final String putHubReqests
            ="insert into hub_responses (HP_P1,HP_P2,HP_P3,HP_P4,HP_P5,HP_HR_HS_NUMBER,HP_ERR_MSG) values (?,?,?,?,?,?,?)";
    private static  PreparedStatement putHubReqestsPS=null;

  
  static public void unregisterForCallback(Hub hub, Connection connection) throws MeteringSessionException{
      OracleConnection conn=null;
      if (connection instanceof OracleConnection)
          conn=(OracleConnection)connection;
      else
          throw new MeteringSessionException("Connection is not instance of OracleConnection");
      DatabaseChangeRegistration dcr=hub.getDCR();
      try {
          conn.unregisterDatabaseChangeNotification(dcr);
      } catch (SQLException ex) {
          throw new MeteringSessionException(ex);
      }
  }
  
  static public void registerForCallback(Hub hub, Connection connection) throws MeteringSessionException{
      OracleConnection conn=null;
      if (connection instanceof OracleConnection)
          conn=(OracleConnection)connection;
      else
          throw new MeteringSessionException("Connection is not instance of OracleConnection");
    Properties prop = new Properties();
    prop.setProperty(OracleConnection.DCN_NOTIFY_ROWIDS,"true");
    prop.setProperty(OracleConnection.NTF_LOCAL_TCP_PORT, "21212");
    prop.setProperty(OracleConnection.DCN_QUERY_CHANGE_NOTIFICATION,"true");
    prop.setProperty(OracleConnection.DCN_IGNORE_DELETEOP,"true");
    prop.setProperty(OracleConnection.DCN_IGNORE_UPDATEOP,"true");        
    DatabaseChangeRegistration dcr=null;
    try
    {
      dcr = conn.registerDatabaseChangeNotification(prop);
      hub.setDCR(dcr);
      // add the listenerr:
      DBChangeNotification list = new DBChangeNotification();
      dcr.addListener(list);
       
      // second step: add objects in the registration:
//      String query=dbcQuery+"'"+ hub.getHubHexId()+"'";
//       Statement stmt = conn.createStatement();
      PreparedStatement stmt = conn.prepareStatement(dbcQuery);
      // associate the statement with the registration:
      ((OracleStatement)stmt).setDatabaseChangeRegistration(dcr);
      stmt.setString(1, hub.getHubHexId());
            
//      ResultSet rs = stmt.executeQuery(query);      
        ResultSet rs = stmt.executeQuery();      
//      while (rs.next())
//      {}
      String[] tableNames = dcr.getTables();
      for(int i=0;i<tableNames.length;i++)
          lgr.debug(tableNames[i]+" is part of the registration.");
      rs.close();
      stmt.close();
      lgr.debug("Registred for query:"+dbcQuery+" ?="+ hub.getHubHexId());
    }
    catch(SQLException ex)
    {
      lgr.warn("Database can not comunicate to Logger Agent due to locked 21212 port on logger machine, "+
              " and database will not succesfully send commands to Logger Agent", ex);
      // if an exception occurs, we need to close the registration in order
      // to interrupt the thread otherwise it will be hanging around.
      if(conn != null)
        try {
          conn.unregisterDatabaseChangeNotification(dcr);
      } catch (SQLException ex1) {
          lgr.debug(ex1.getMessage());
      }
        //throw new MeteringSessionException(ex);
    }               
}
  


    @Override
    public void onDatabaseChangeNotification(DatabaseChangeEvent dce) {
        lgr.debug(dce.toString());
        Connection conn = null;
        try {
            conn = DBUtils.createDBConnection();
            getHubReqestsPS = conn.prepareStatement(getHubReqests);
            putHubReqestsPS = conn.prepareStatement(putHubReqests);
            QueryChangeDescription[] queryChangeDescription = dce.getQueryChangeDescription();
            if (queryChangeDescription==null){
                lgr.error("queryChangeDescription is null");
                return;
            }
            for(QueryChangeDescription qcd:queryChangeDescription){
                TableChangeDescription[] tableChangeDescription = qcd.getTableChangeDescription();
                if (tableChangeDescription==null){
                    lgr.error("tableChangeDescription is null");
                    return;
                }       
                for(TableChangeDescription tcd:tableChangeDescription){
                      RowChangeDescription[] rowChangeDescription = tcd.getRowChangeDescription();                      
                      if (tableChangeDescription==null){
                        lgr.error("tableChangeDescription is null");
                        return;
                      }
                
                      for(RowChangeDescription rcd:rowChangeDescription){
                          RowChangeDescription.RowOperation rowOperation = rcd.getRowOperation();
                          if (rowOperation!=RowChangeDescription.RowOperation.INSERT)
                              return;
                          ROWID rowid = rcd.getRowid();
                          HubRequest hr = getHubRequestFromDB(getHubReqestsPS, rowid);
                          HubResponse hp=serviceHubRequest(hr);
                          insertHubResponseIntoDB(putHubReqestsPS, hp);      
                          conn.commit();
                      }
                    
                }
            }
        } catch (MeteringSessionException ex) {
            lgr.error(ex.getMessage());
        } catch (SQLException ex) {
            lgr.error(ex.getMessage());
        }
        finally{
            if (conn!=null)
                try {
                conn.close();
            } catch (SQLException ex) {
                ; //ignore it
            }
        }
        
    }

    protected HubResponse serviceHubRequest(HubRequest hr) {
        HubResponse hp = new HubResponse();
        hp.setHexHubId(hr.getHexHubId());
        hp.getParameters()[0]="OK";
        
        try {
            if ("downloadMeasurmentsFromHub".equals(hr.getCommand())){
                Date date = parseDate(hr.getParameters()[0]);
//                HubSessionService.downloadMeasurmentsFromHub(hr.getHexHubId(), date);
            }
            else if("downloadMeasurmentsFromLogger".equals(hr.getCommand())){
                Date date = parseDate(hr.getParameters()[0]);
//                HubSessionService.downloadMeasurmentsFromLogger(hr.getHexHubId(), date);
            }
            else if("intervalHubFlashMemoryMode".equals(hr.getCommand())){
                Date startTime = parseDate(hr.getParameters()[0]);
                Date endTime = parseDate(hr.getParameters()[1]);
                boolean enable = parseBool(hr.getParameters()[2]);
//                HubSessionService.intervalHubFlashMemoryMode(hr.getHexHubId(), startTime, endTime, enable);
            }
            else if("overwriteHubFlashMemoryMode".equals(hr.getCommand())){
                boolean enable = parseBool(hr.getParameters()[0]);
//                HubSessionService.overwriteHubFlashMemoryMode(hr.getHexHubId(), enable);                
            }
            else if("registerMeasurer".equals(hr.getCommand())){
                String measurerNo = hr.getParameters()[0];
//                HubSessionService.registerMeasurer(hr.getHexHubId(), measurerNo);
            }
            else if("unregisterMeasurer".equals(hr.getCommand())){
                String measurerNo = hr.getParameters()[0];
//                HubSessionService.unregisterMeasurer(hr.getHexHubId(), measurerNo);
            }
            else if("measurerRadio".equals(hr.getCommand())){
                String measurerNo = hr.getParameters()[0];
                boolean enable = parseBool(hr.getParameters()[1]);
//                HubSessionService.measurerRadio(hr.getHexHubId(), measurerNo, enable);
            }
        } catch (MeteringSessionException ex) {
            hp.getParameters()[0]="Error";
            hp.setErrMsg(ex.getMessage().substring(1, 2000));
            
        }
        return hp;
    }
    
    protected static Date parseDate(String dateString) throws MeteringSessionException{
        SimpleDateFormat format =
            new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        Date parsed =null;
        // See if we can parse the output of Date.toString()
        try {
             parsed = format.parse(dateString);
        }
        catch(ParseException pe) {
            throw new MeteringSessionException(pe);
        }
        return parsed;
    }

    protected static boolean parseBool(String bool) throws MeteringSessionException{
        if ("TRUE".equals(bool))
            return true;
        else if ("FALSE".equals(bool))
            return false;
        else 
            throw new MeteringSessionException("Can't parse bool value:"+bool);
    }

    private HubRequest getHubRequestFromDB(PreparedStatement getHubReqestsPS, ROWID rowid) throws SQLException {
        getHubReqestsPS.setRowId(1, rowid);
        HubRequest hr =new HubRequest();
        ResultSet rs = getHubReqestsPS.executeQuery();
        if (rs.next()){
          hr.setHexHubId(rs.getString("HR_HS_NUMBER"));
          hr.setCommand(rs.getString("HR_COMMAND"));
          String[]parameters=hr.getParameters();
          for (int i=1; i<=10; i++)
              parameters[i-1]=rs.getString(i);
        }
        return hr;
    }

    private void insertHubResponseIntoDB(PreparedStatement putHubReqestsPS, HubResponse hp) throws SQLException {
        for (int i=1; i<=5; i++)
            putHubReqestsPS.setString(i, hp.getParameters()[i-1]);
        putHubReqestsPS.setString(6, hp.getHexHubId());
        putHubReqestsPS.setString(7, hp.getErrMsg());
        putHubReqestsPS.executeUpdate();
    }
    
    public static void  main(String[]arg) throws MeteringSessionException, InterruptedException{
        PropertyConfigurator.configure(HubSessionManager.class.getResource("log4j.properties"));

        Connection conn = DBUtils.createDBConnection();
        Hub hub = new Hub("1", "ComX");
        
        DBChangeNotification.registerForCallback(hub, conn);
        Thread.sleep(1000*120);
        DBChangeNotification.unregisterForCallback(hub, conn);
        
    }
}
