import java.sql.*;
import java.io.*;

public class ReadDb {
    public static void main(String[] args) throws Exception {
        Class.forName("org.sqlite.JDBC");
        Connection c = DriverManager.getConnection("jdbc:sqlite:" + args[0]);
        DatabaseMetaData md = c.getMetaData();
        try (ResultSet rs = md.getTables(null, null, "%", null)) {
            while (rs.next()) System.out.println("TABLE " + rs.getString(3));
        }
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT count(*) FROM bitcoin")) {
            System.out.println("COUNT " + rs.getLong(1));
        }
        try (Statement st = c.createStatement();
             ResultSet rs = st.executeQuery("SELECT sum(value) FROM bitcoin")) {
            System.out.println("SUM_SATS " + rs.getLong(1));
        }
        try (PrintWriter w = new PrintWriter(new OutputStreamWriter(
                new FileOutputStream(args[1]), "UTF-8"))) {
            try (Statement st = c.createStatement();
                 ResultSet rs = st.executeQuery("SELECT _id, value FROM bitcoin")) {
                while (rs.next()) w.println(rs.getString(1) + "," + rs.getLong(2));
            }
        }
        c.close();
    }
}
