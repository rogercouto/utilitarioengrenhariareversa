package erp.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

public class Type {

	private String dbType;
	private Class<?> javaType;
	
	public Type(String dbType, Class<?> javaType) {
		super();
		this.dbType = dbType;
		this.javaType = javaType;
	}
	
	public String getDbType() {
		return dbType;
	}
	public Class<?> getJavaType() {
		return javaType;
	}

	public String getJavaSQLSufix(){
		if (javaType.equals(String.class))
			return "String";
		else if (javaType.equals(Integer.TYPE))
			return "Int";
		else if (javaType.equals(Long.TYPE))
			return "Long";
		else if (javaType.equals(Float.TYPE))
			return "Float";
		else if (javaType.equals(Double.TYPE))
			return "Double";
		else if (javaType.equals(Boolean.TYPE))
			return "Boolean";
		else if (javaType.equals(LocalDate.class))
			return "Date";
		else if (javaType.equals(LocalTime.class))
			return "Time";
		else if (javaType.equals(LocalDateTime.class))
			return "Timestamp";
		else
			return "Object";
	}
	
	public boolean isPrimitive(){
		if (javaType.equals(Integer.TYPE)
		||javaType.equals(Long.TYPE)
		||javaType.equals(Float.TYPE)
		||javaType.equals(Double.TYPE)
		||javaType.equals(Boolean.TYPE))
			return true;
		return false;
	}
}
