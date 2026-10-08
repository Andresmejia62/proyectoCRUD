package connection;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DatabaseConnection{

    private static final String URL = "jdbc:postgresql://localhost:5432/dbventas";
    private static final String USER = "andresmejia";
    private static final String PASSWORD = "amejia62";

    private DatabaseConnection(){



    }

    public static Connection getConnection () throws SQLException {
        return DriverManager.getConnection(URL,USER,PASSWORD);
    }
}
