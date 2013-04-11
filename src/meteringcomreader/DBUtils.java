package meteringcomreader;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Zawiera pomocnicze funkcje statyczne operujące na bazie danych
 * @author Juliusz Jezierski
 */
public class DBUtils {
    /**
     * Tekst polecenia ustawiającego strefę czasową polecenia na UTC.
     */
    protected static String setTimezoneToUTC="alter session set time_zone='00:00'";
    /**
     * Opis połączenia do bazy danych.
     */
    protected static String connDesc = "jdbc:oracle:thin:@//admlab1.cs.put.poznan.pl:12121/XE";
    /**
     * Nazwa użytkownika bazy danych.
     */
    protected static String user = "meter";
    /**
     * Hasło użytkownika bazy danych.
     */
    protected static String pass = "m3t3ring";

    /**
     * Tworzy połączenie do bazy danych i ustawia strefę czasową na 
     * UTC.
     * @return zwraca utworzone połączenie do bazy danych
     * @throws MeteringSessionException w przypadku nieznalezienia klasy drivera JDBC
     * lub zgłoszenia SQLException
     */
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
