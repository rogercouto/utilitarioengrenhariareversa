package model.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.Properties;

public class SessionManager {

	public static boolean printConnections = true;
	
	private static String url;
	private static String username;
	private static String password;
	
	private static Connection connection = null;
	
	private static Connection createConnection(){
		File fileProp = new File("db.properties");
		Properties prop = new Properties();
		FileInputStream in;
		try {
			in = new FileInputStream(fileProp);
			prop.load(in);
			url = prop.getProperty("jdbc.url");
			if (prop.getProperty("jdbc.database") != null)
				url += prop.getProperty("jdbc.database");
			username = prop.getProperty("jdbc.username");
			password = prop.getProperty("jdbc.password");
			connection = DriverManager.getConnection(url, username, password);
			connection.setAutoCommit(false);
			if (printConnections)
				System.err.println("Connected to database!");
			return connection;
		} catch (IOException | SQLException e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	public static boolean testConnection(){
		File fileProp = new File("db.properties");
		Properties prop = new Properties();
		FileInputStream in;
		try {
			in = new FileInputStream(fileProp);
			prop.load(in);
			url = prop.getProperty("jdbc.url");
			if (prop.getProperty("jdbc.database") != null)
				url += prop.getProperty("jdbc.database");
			username = prop.getProperty("jdbc.username");
			password = prop.getProperty("jdbc.password");
			connection = DriverManager.getConnection(url, username, password);
			connection.setAutoCommit(false);
			if (printConnections)
				System.err.println("Connected to database!");
			if (connection != null){
				if (printConnections)
					System.err.println("Disconnected to database!");
				connection.close();
				connection = null;
				return true;
			}
		} catch (IOException | SQLException e) {
			e.printStackTrace();
		}
		return false;
	}
	
	public static Session createSession(){
		if (connection != null)
			return new Session(connection, false);
		else{
			return new Session(createConnection(), true);
		}
	}
	
	public static void closeConnection(){
		try {
			connection.close();
			connection = null;
			if (printConnections)
				System.err.println("Disconnected to database!");
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
		
	}
	
}
