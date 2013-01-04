/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import java.sql.*;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 *
 * @author Juliusz
 */
abstract public class SessionDBInserterHelper {
    static private PreparedStatement registerHubPS=null;
    static private PreparedStatement unregisterHubPS=null;
    static private PreparedStatement unregisterAllHubsPS=null;

    protected static String registerHubSQL="{call metering_hub_session.register_hub(?,?,?)}";            
    protected static String unregisterHubSQL="{call metering_hub_session.unregister_hub(?,?)}";
    protected static String unregisterAllHubsSQL="{call metering_hub_session.unregister_all_hubs(?)}";
    
//    protected static Connection conn;


    static public void registerHubs(Hubs hubs) throws MeteringSessionException{
       Connection conn = null;
        try{
            conn = DBUtils.createDBConnection();
            registerHubPS=conn.prepareCall(registerHubSQL);
            Iterator it =hubs.entrySet().iterator();

            while (it.hasNext()) {
                Map.Entry<String, Hub> pairs = (Map.Entry<String, Hub>)it.next();
                Hub hub = pairs.getValue();
                String hubid=hub.getHubHexId();
                registerHubPS.setString(1, hubid);
                registerHubPS.setString(2, hub.getComPortName());
                registerHubPS.setString(3, "connected");
                registerHubPS.execute();
            }
            conn.commit();
            registerHubPS.close();
            registerHubPS=null;
            
        }
        catch (SQLException ex) {
                throw new MeteringSessionException(ex);  
        }
        finally{
            try {
                if(conn!=null)
                    conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(SessionDBInserterHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }


       

   static public void registerHub(Hub hub) throws MeteringSessionException{
       Connection conn = null;
        try{
            conn = DBUtils.createDBConnection();
            registerHubPS=conn.prepareCall(registerHubSQL);
            String hubid=hub.getHubHexId();
            registerHubPS.setString(1, hubid);
            registerHubPS.setString(2, hub.getComPortName());
            registerHubPS.setString(3, "connected");            
            registerHubPS.execute();
            conn.commit();
            registerHubPS.close();
            registerHubPS=null;
        }
        catch (SQLException ex) {
            int errCode=ex.getErrorCode();
            if (errCode==1){ //ORA-00001: unique constraint (METER.MEASUSER_SESSION_PK) violated
                ; //ignore it
            }
            else {
                throw new MeteringSessionException(ex);
            }    
        }
        finally{
            try {
                if(conn!=null)
                    conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(SessionDBInserterHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
            
    static public void unregisterAllHubs()throws MeteringSessionException{
          Connection conn = null;
         try{
            conn = DBUtils.createDBConnection();
            if(conn!=null){
                unregisterAllHubsPS=conn.prepareCall(unregisterAllHubsSQL);
                unregisterAllHubsPS.setString(1, "disconnected");
                unregisterAllHubsPS.execute();
                conn.commit();
                unregisterAllHubsPS.close();
                unregisterAllHubsPS=null;
            }
         }
        catch (SQLException ex) {
             throw new MeteringSessionException(ex);
         }
         finally{
            try {
                if(conn!=null)
                    conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(SessionDBInserterHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    static public void unregisterHub(Hub hub)throws MeteringSessionException{
            String hubid=hub.getHubHexId();
            unregisterHub(hubid);
    }    
    
    
    
    static public void unregisterHub(String hubid)throws MeteringSessionException{
         Connection conn = null;
         try{
            conn = DBUtils.createDBConnection();

            unregisterHubPS=conn.prepareCall(unregisterHubSQL);
            unregisterHubPS.setString(1, hubid);
            unregisterHubPS.setString(2, hubid);
            unregisterHubPS.execute();
            conn.commit();
            unregisterHubPS.close();
            unregisterHubPS=null;
         }
        catch (SQLException ex) {
             throw new MeteringSessionException(ex);
         }
         finally{
            try {
                if(conn!=null)
                    conn.close();
            } catch (SQLException ex) {
                Logger.getLogger(SessionDBInserterHelper.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }
    
}
