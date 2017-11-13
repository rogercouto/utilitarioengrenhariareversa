package erp.db;

import java.sql.Connection;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

import erp.export.FileData;
import erp.model.Type;

public class MysqlServer implements Server {

	private ConnectionFactory connectionFactory;

	@Override
	public Type getType(String dbTypeName, int size) {
		//System.out.println(dbTypeName);
		if (dbTypeName.compareTo("INT") == 0)
			return new Type(dbTypeName, Integer.class);
		else if (dbTypeName.compareTo("BIGINT") == 0)
			return new Type(dbTypeName, Long.class);
		else if (dbTypeName.compareTo("FLOAT") == 0)
			return new Type(dbTypeName, Float.class);
		else if (dbTypeName.compareTo("DOUBLE") == 0)
			return new Type(dbTypeName, Double.class);
		else if (dbTypeName.compareTo("VARCHAR") == 0)
			return new Type(dbTypeName, String.class);
		else if (dbTypeName.compareTo("CHAR") == 0)
			return new Type(dbTypeName, size == 1 ? Character.class : String.class);
		else if (dbTypeName.compareTo("TEXT") == 0)
			return new Type(dbTypeName, String.class);
		else if (dbTypeName.compareTo("DATE") == 0)
			return new Type(dbTypeName, LocalDate.class);
		else if (dbTypeName.compareTo("TIME") == 0)
			return new Type(dbTypeName, LocalTime.class);
		else if (dbTypeName.compareTo("TIMESTAMP") == 0 || dbTypeName.compareTo("DATETIME") == 0)
			return new Type(dbTypeName, LocalDateTime.class);
		else if (dbTypeName.compareTo("BOOL") == 0)
			return new Type(dbTypeName, Boolean.class);
		else if (dbTypeName.compareTo("TINYINT") == 0 || dbTypeName.compareTo("BIT") == 0)
			return new Type(dbTypeName, Boolean.TYPE);
		else if (dbTypeName.compareTo("DECIMAL") == 0 || dbTypeName.compareTo("DECIMAL UNSIGNED") == 0 )
			return new Type(dbTypeName, Number.class);
		else if (dbTypeName.compareTo("ENUM") == 0)
			return new Type(dbTypeName, String.class);
		return new Type("UNKNOWN", Object.class);
	}

	@Override
	public String getKeyGen() {
		return "SELECT LAST_INSERT_ID()";
	}

	@Override
	public void setProperties(String url, String username, String password) {
		connectionFactory = new ConnectionFactory(url, username, password);
	}

	@Override
	public Connection getConnection() {
		return connectionFactory.getConnection();
	}

	@Override
	public FileData getProperties() {
		StringBuilder builder = new StringBuilder();
		builder.append("jdbc.url=").append(connectionFactory.getUrl()).append("\n");
		builder.append("jdbc.username=").append(connectionFactory.getUsername()).append("\n");
		builder.append("jdbc.password=").append(connectionFactory.getPassword() != null ? connectionFactory.getPassword() : "");
		return new FileData("db.properties", builder.toString());
	}


}
