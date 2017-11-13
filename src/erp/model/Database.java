package erp.model;

import java.util.LinkedList;
import java.util.List;

public class Database {

	private String name;
	private List<Table> tables = new LinkedList<>();

	public Database(String name) {
		super();
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public List<Table> getTables() {
		return tables;
	}

	public void addTable(Table table){
		tables.add(table);
	}

	public Table getTable(String name){
		for (Table table : tables) {
			if (table.getName().compareTo(name) == 0)
				return table;
		}
		return null;
	}

	public List<Table> getOtherTables(Table remTable){
		List<Table> list = new LinkedList<>();
		for (Table t : tables) {
			if (!t.equals(remTable))
				list.add(t);
		}
		return list;
	}

	public void print(){
		for (Table table : tables) {
			System.out.println(table.getName());
			for (Column column : table.getColumns()) {
				System.out.print("- "+column.getName()+" : "+column.getType().getDbType());
				if (column.isPrimaryKey())
					System.out.print(" PK");
				if (!column.isNullable())
					System.out.print(" NN");
				if (column.isAutoIncrement())
					System.out.print(" AA");
				if (column.isUnique())
					System.out.print(" U");
				if (column.isForeignKey()){
					System.out.print(" FK");
					System.out.print(" (Reference: "+column.getForeignKey().getClassName()+")");
				}
				System.out.println();
			}
		}
	}

	public void printJava(){
		for (Table table : tables) {
			if (!table.isNtoN()){
				System.out.println(table.getClassName());
				for (Column column : table.getColumns()) {
					if (column.isForeignKey()){
						System.out.print("- "+column.getForeignKey().getClassName()+" : "+column.getForeignKey().getObjectName());
					}else{
						System.out.print("- "+column.getAtributeName(table.getName())+" : "+column.getType().getJavaType().getSimpleName());
					}
					System.out.println();
				}
				for (Reference ref : table.getReferences()) {
					System.out.println("- list"+ref.get().getClassName()+" : List<"+ref.get().getClassName()+">");
				}
			}
		}
	}

}
