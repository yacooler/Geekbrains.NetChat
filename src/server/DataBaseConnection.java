package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DataBaseConnection {
    // JDBC driver name and database URL
    static final String JDBC_DRIVER = "org.h2.Driver";
    static final String DB_URL = "jdbc:h2:~/netChat";
    static final String DB_USER = "sa";
    static final String DB_PASS = "";

    private static Connection dataBaseConnection = null;

    public static Connection getConnection() throws ClassNotFoundException, SQLException {
        if (dataBaseConnection == null){
            Class.forName(JDBC_DRIVER);
            dataBaseConnection = DriverManager.getConnection(DB_URL,DB_USER,DB_PASS);
        }
        return dataBaseConnection;
    }

    public static void closeConnection(){
        try {
            if(dataBaseConnection !=null) dataBaseConnection.close();
        } catch(SQLException se){
            throw new RuntimeException("Ошибка закрытия подключения к БД", se);
        }
    }

}
