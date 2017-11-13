package model.data;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;


public class Session {

	private Connection connection;
	private boolean canClose;
	
	protected Session(Connection connection, boolean canClose){
		this.connection = connection;
		this.canClose = canClose;
	}
	
	public void commit(){
		try {
			connection.commit();
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}
	
	public void rollback(){
		try {
			connection.rollback();
		} catch (SQLException e) {
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}
	
	public PreparedStatement prepareStatement(String sql) throws SQLException{
		return connection.prepareStatement(sql);
	}
	
	public Statement createStatement() throws SQLException{
		return connection.createStatement();
	}
	
	@Deprecated
	public Connection getConnection(){
		return connection;
	}
	
	public void close(){
		if (canClose)
			SessionManager.closeConnection();
	}
	
}
