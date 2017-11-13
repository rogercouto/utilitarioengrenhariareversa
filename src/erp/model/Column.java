package erp.model;

public class Column {

	private String name;
	private Type type;
	private int size;
	private boolean primaryKey;
	private boolean autoIncrement;
	private boolean nullable;
	private boolean unique;
	private Table foreignKey;

	public Column() {
		super();
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

	public boolean isPrimaryKey() {
		return primaryKey;
	}

	public void setPrimaryKey(boolean primaryKey) {
		this.primaryKey = primaryKey;
	}

	public boolean isAutoIncrement() {
		return autoIncrement;
	}

	public void setAutoIncrement(boolean autoIncrement) {
		this.autoIncrement = autoIncrement;
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

	public boolean isForeignKey(){
		return foreignKey != null;
	}

	public Table getForeignKey() {
		return foreignKey;
	}

	public void setForeignKey(Table foreignKey) {
		this.foreignKey = foreignKey;
	}

	private boolean simplify(String name, String tableName){
		if (!primaryKey)
			return false;
		if (!name.contains("_id") && !name.contains("id_"))
			return false;
		if (tableName != null && name.toLowerCase().contains(tableName.toLowerCase()))
			return true;
		return false;
	}

	private String getJavaName(boolean startUpper, String tableName){
		if (simplify(name, tableName)){
			if (startUpper)
				return "Id";
			else
				return "id";
		}
		char ca[] = name.toCharArray();
		StringBuilder builder = new StringBuilder();
		boolean upperNext = startUpper;
		boolean first = true;
		for (char c : ca) {
			if ((first && Character.isLetter(c)) || Character.isLetterOrDigit(c)){
				if (upperNext){
					builder.append(Character.toUpperCase(c));
					upperNext = false;
				}else{
					builder.append(c);
				}
			}
			if (c == '_')
				upperNext = true;
		}
		return builder.toString();
	}

	public String getAtributeName(String tableName){
		if (isForeignKey())
			return foreignKey.getObjectName();
		return getJavaName(false, tableName);
	}

	public String getAtributeType(){
		if (isForeignKey())
			return foreignKey.getClassName();
		return type.getJavaType().getSimpleName();
	}

	public String getMethodSufix(String tableName){
		if (isForeignKey())
			return foreignKey.getClassName();
		return getJavaName(true, tableName);
	}

	public boolean isNullable() {
		return nullable;
	}

	public void setNullable(boolean nullable) {
		this.nullable = nullable;
	}

}
