package resource.dao;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DbUtils {

    public final static String MYSQL_DRIVER = "com.mysql.jdbc.Driver";
    public final static String MARIADB_DRIVER = "org.mariadb.jdbc.Driver";
    public final static String MYSQL_URL = "jdbc:mysql://127.0.0.1:3306/test";
    public final static String MARIADB_B_URL = "jdbc:mariadb://192.168.196.100:3306/test";
    public final static String MARIADB_C_URL = "jdbc:mariadb://192.168.196.101:3306/test";
    public final static String DB_USER = "root";

    public static Connection getConn(String driver, String dbUrl, String dbPwd) {
        Connection conn = null;
        try {
            Class.forName(driver);
            conn = DriverManager.getConnection(dbUrl, DB_USER, dbPwd);
        } catch (ClassNotFoundException | SQLException e) {
            e.printStackTrace();
        }
        return conn;
    }
    public static void closeConn(Connection conn) {
        try {
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


}
