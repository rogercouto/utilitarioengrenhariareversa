package erp.db;

import java.sql.Connection;

import erp.export.FileData;
import erp.model.Type;

public interface Server {
	
	public Type getType(String dbTypeName, int size);
	
	public String getKeyGen();
	
	public void setProperties(String url, String userName, String password);
	
	public Connection getConnection();
	
	public FileData getProperties();

}
