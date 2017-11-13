package erp.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class ConnectionFactory {

	private String driver = null;
	private String url;
	private String username;
	private String password;
	
	public ConnectionFactory(String url, String username, String password) {
		super();
		this.url = url;
		this.username = username;
		if (!password.isEmpty())
			this.password = password;
	}
	
	public ConnectionFactory(String driver, String url, String username, String password) {
		super();
		this.driver = driver;
		this.url = url;
		this.username = username;
		if (!password.isEmpty())
			this.password = password;
	}
	
	public void testConnection() throws SQLException{
		try {
			if (driver != null)
				Class.forName(driver);
			Connection connection = DriverManager.getConnection(url, username, password);
			connection.close();
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	public Connection getConnection(){
		try {
			if (driver != null)
				Class.forName(driver);
			return DriverManager.getConnection(url, username, password);
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}

	public String getDriver() {
		return driver;
	}

	public String getUrl() {
		return url;
	}

	public String getUsername() {
		return username;
	}

	public String getPassword() {
		return password;
	}
	
}
