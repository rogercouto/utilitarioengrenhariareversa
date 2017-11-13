package erp.model;

import java.util.LinkedList;
import java.util.List;

public class Table {

	private String name;
	private List<Column> columns = new LinkedList<>();
	private List<Reference> references = new LinkedList<>();

	public Table(String name) {
		super();
		this.name = name;
	}
	public String getName() {
		return name;
	}
	public void addColumn(Column column){
		columns.add(column);
	}
	public Column getColumn(int index){
		return columns.get(index);
	}
	public List<Column> getColumns() {
		return columns;
	}
	public void addReference(Reference reference){
		references.add(reference);
	}
	public Reference getReference(int index){
		return references.get(index);
	}
	public List<Reference> getReferences() {
		return references;
	}
	public int getNtoNRefCount(){
		int c = 0;
		for (Reference reference : references) {
			if (reference.isNtoN())
				c++;
		}
		return c;
	}

	public boolean isNtoN(){
		int count = 0;
		for (Column column : columns) {
			if (column.isPrimaryKey())
				count++;
		}
		return count > 1 && count == columns.size();
	}

	//TODO
	public Column getPrimaryKey(){
		if (!isNtoN()){
			for (Column column : columns) {
				if (column.isPrimaryKey())
					return column;
			}
		}
		return null;
	}

	public List<Column> getPrimaryKeys(){
		List<Column> pks = new LinkedList<>();
		if (!isNtoN()){
			for (Column column : columns) {
				if (column.isPrimaryKey())
					pks.add(column);
			}
		}
		return pks;
	}

	public String getJavaName(boolean startUpper){
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

	public String getClassName(){
		return getJavaName(true);
	}

	public String getFileName(){
		return getJavaName(true).concat(".java");
	}

	public String getDAOFileName(){
		return getJavaName(true).concat("DAO.java");
	}

	public String getObjectName(){
		return getJavaName(false);
	}

	public Column getColumn(String name){
		for (Column column : columns) {
			if (column.getName().compareTo(name) == 0)
				return column;
		}
		return null;
	}

	public boolean haveForeignKeys(){
		for (Column column : columns) {
			if (column.isForeignKey())
				return true;
		}
		return false;
	}

	public boolean importList(){
		for (Reference reference : references) {
			if (reference.createList())
				return true;
		}
		return false;
	}

	public boolean importSession(){
		return references.size() > 0;
	}

	public void createLists(boolean create){
		for (Reference reference : references)
			reference.setCreateList(create);
	}

	public List<Table> getFks(){
		List<Table> fks = new LinkedList<>();
		List<Table> subFks = new LinkedList<>();
		int count = 0;
		for (Column column : columns) {
			if (column.isForeignKey()){
				fks.add(column.getForeignKey());
				count++;
			}
		}
		for (Table fk : fks) {
			if (count > 0)
				subFks.addAll(fk.getFks());
		}
		for (Table table : subFks) {
			fks.add(table);
		}
		return fks;
	}

}
