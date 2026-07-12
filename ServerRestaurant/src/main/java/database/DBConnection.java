package database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DBConnection {

    private static final String URL =
            "jdbc:sqlserver://localhost:1433;"
            + "databaseName=QuanLiNhaHang;"
            + "encrypt=true;"
            + "trustServerCertificate=true;";

    private static final String USER = "sa";

    private static final String PASSWORD = "123456";

    public static Connection getConnection() {

        Connection conn = null;

        try {

            Class.forName(
                "com.microsoft.sqlserver.jdbc.SQLServerDriver"
            );

            conn = DriverManager.getConnection(
                    URL,
                    USER,
                    PASSWORD
            );

            System.out.println(
                "Ket noi SQL Server thanh cong!"
            );

        } catch (ClassNotFoundException e) {

            System.out.println(
                "Khong tim thay JDBC Driver!"
            );

            e.printStackTrace();

        } catch (SQLException e) {

            System.out.println(
                "Ket noi SQL Server that bai!"
            );

            e.printStackTrace();
        }

        return conn;
    }
}