/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package meteringcomreader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 *
 * @author Juliusz
 */
public class DBUtils {
    protected static String setTimezoneToUTC="alter session set time_zone='00:00'";
    protected static String connDesc = "jdbc:oracle:thin:@//localhost:1521/XE";
    protected static String user = "meter";
    protected static String pass = "m3t3ring";

    static Connection createDBConnection() throws MeteringSessionException{
        Connection conn=null;
        try {
            Class.forName("oracle.jdbc.OracleDriver");
            conn = DriverManager.getConnection(connDesc, user, pass);
            conn.setAutoCommit(false);
            conn.createStatement().execute(setTimezoneToUTC);
        } catch (SQLException ex) {
            throw new MeteringSessionException(ex);
        } catch (ClassNotFoundException ex) {
            throw new MeteringSessionException(ex);
        }
        return conn;
    }
    
}
