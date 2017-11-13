package erp.export;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.LinkedList;
import java.util.List;

import erp.model.Column;
import erp.model.Database;
import erp.model.Reference;
import erp.model.Table;

public class ModelExporter {

	private Database database;

	public ModelExporter(Database database){
		super();
		this.database = database;
	}

	private List<String> getImports(Table table){
		List<String> list = new LinkedList<>();
		boolean useDate, useTime, useDateTime;
		useDate = useTime = useDateTime = false;
		for (Column column : table.getColumns()) {
			if (column.getType().getJavaType().equals(LocalDate.class))
				useDate = true;
			if (column.getType().getJavaType().equals(LocalTime.class))
				useTime = true;
			if (column.getType().getJavaType().equals(LocalDateTime.class))
				useDateTime = true;
		}
		if (useDate)
			list.add("import java.time.LocalDate;");
		if (useTime)
			list.add("import java.time.LocalTime;");
		if (useDateTime)
			list.add("import java.time.LocalDateTime;");
		if (table.importList())
			list.add("import model.data.DataList;");
		for (Reference reference : table.getReferences()) {
			StringBuilder builder = new StringBuilder();
			builder.append("import model.dao.");
			builder.append(reference.get().getClassName());
			builder.append("DAO;");
			list.add(builder.toString());
		}
		list.add("import model.data.Entity;");
		return list;
	}

	public String getGetterMethod(Column column, String tableName){
		StringBuilder builder = new StringBuilder();
		builder.append("\tpublic ").append(column.getAtributeType()).append(" ");
		if (column.getType().getJavaType().equals(Boolean.class) || column.getType().getJavaType().equals(Boolean.TYPE))
			builder.append("is");
		else
			builder.append("get");
		builder.append(column.getMethodSufix(tableName)).append("(){\n");
		builder.append("\t\treturn ").append(column.getAtributeName(tableName)).append(";\n");
		builder.append("\t}\n");
		return builder.toString();
	}

	public String getSetterMethod(Column column, String tableName){
		StringBuilder builder = new StringBuilder();
		builder.append("\tpublic void ").append("set").append(column.getMethodSufix(tableName)).append("(")
			.append(column.getAtributeType()).append(' ').append(column.getAtributeName(tableName)).append("){\n");
		builder.append("\t\tthis.").append(column.getAtributeName(tableName)).append(" = ").append(column.getAtributeName(tableName)).append(";\n");
		builder.append("\t}\n");
		return builder.toString();
	}

	public String getCheckMethod(Reference reference, Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("\tprivate void checkList").append(reference.get().getClassName()).append("(){\n");
		builder.append("\t\tif (list").append(reference.get().getClassName()).append(" == null){\n");
		builder.append("\t\t\tif (").append(table.getPrimaryKey().getAtributeName(table.getName())).append(" == ");
		if (table.getPrimaryKey().getType().isPrimitive())
			builder.append("0");
		else
			builder.append("null");
		builder.append(")\n");
		builder.append("\t\t\t\tlist").append(reference.get().getClassName()).append(" = new DataList<").append(reference.get().getClassName()).append(">();\n");
		builder.append("\t\t\telse\n");
		builder.append("\t\t\t\tlist").append(reference.get().getClassName());
		builder.append(" = new ").append(reference.get().getClassName()).append("DAO().getList(this);\n");
		builder.append("\t\t}\n\t}\n");
		return builder.toString();
	}

	public String getGetterMethod(Reference reference){
		StringBuilder builder = new StringBuilder();
		builder.append("\tpublic DataList<");
		builder.append(reference.get().getClassName());
		builder.append("> getList");
		builder.append(reference.get().getClassName());
		builder.append("(){\n");
		builder.append("\t\tcheckList").append(reference.get().getClassName()).append("();\n");
		builder.append("\t\treturn list");
		builder.append(reference.get().getClassName());
		builder.append(";\n\t}\n");
		return builder.toString();
	}

	public String getSetterMethod(Reference reference){
		StringBuilder builder = new StringBuilder();
		builder.append("\tpublic void setList");
		builder.append(reference.get().getClassName());
		builder.append("(DataList<");
		builder.append(reference.get().getClassName());
		builder.append("> list");
		builder.append(reference.get().getClassName());
		builder.append("){\n");
		builder.append("\t\tthis.list");
		builder.append(reference.get().getClassName());
		builder.append(" = list");
		builder.append(reference.get().getClassName());
		builder.append(";\n\t}\n");
		return builder.toString();
	}

	public String getAddMethod(Reference reference, Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("\tpublic void add");
		builder.append(reference.get().getClassName());
		builder.append("(");
		builder.append(reference.get().getClassName());
		builder.append(' ');
		builder.append(reference.get().getObjectName());
		builder.append("){\n");
		builder.append("\t\tcheckList").append(reference.get().getClassName()).append("();\n");
		if (reference.getDestiny() == null){
			builder.append("\t\t");
			builder.append(reference.get().getObjectName());
			builder.append(".set");
			builder.append(table.getClassName());
			builder.append("(this);\n");
		}
		builder.append("\t\tlist");
		builder.append(reference.get().getClassName());
		builder.append(".add(");
		builder.append(reference.get().getObjectName());
		builder.append(");\n\t}\n");
		return builder.toString();
	}

	public String getSetMethod(Reference reference, Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("\tpublic void set");
		builder.append(reference.get().getClassName());
		builder.append("(int index, ");
		builder.append(reference.get().getClassName());
		builder.append(' ');
		builder.append(reference.get().getObjectName());
		builder.append("){\n");
		builder.append("\t\tcheckList").append(reference.get().getClassName()).append("();\n");
		if (reference.getDestiny() == null){
			builder.append("\t\t");
			builder.append(reference.get().getObjectName());
			builder.append(".set");
			builder.append(table.getClassName());
			builder.append("(this);\n");
		}
		builder.append("\t\tlist");
		builder.append(reference.get().getClassName());
		builder.append(".set(index, ");
		builder.append(reference.get().getObjectName());
		builder.append(");\n\t}\n");
		return builder.toString();
	}

	public String getRemoveMethod(Reference reference){
		StringBuilder builder = new StringBuilder();
		builder.append("\tpublic void remove");
		builder.append(reference.get().getClassName());
		builder.append("(int index){\n");
		builder.append("\t\tcheckList").append(reference.get().getClassName()).append("();\n");
		builder.append("\t\tlist");
		builder.append(reference.get().getClassName());
		builder.append(".remove(index);\n\t}\n");
		return builder.toString();
	}

	public String getEqualsMethod(Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("\t@Override\n\tpublic boolean equals(Object object){\n");
		builder.append("\t\tif (object == null)\n\t\t\treturn false;\n");
		builder.append("\t\tif (object instanceof ").append(table.getClassName()).append(") {\n");
		builder.append("\t\t\t").append(table.getClassName()).append(' ').append(table.getObjectName())
			.append(" = (").append(table.getClassName()).append(") object;\n");
		Column pk = table.getPrimaryKey();
		if (pk.getType().isPrimitive()){
			builder.append("\t\t\treturn ").append(pk.getAtributeName(table.getName()));
			builder.append(" == ").append(table.getObjectName())
			.append(".get").append(pk.getMethodSufix(table.getName())).append("();\n");
		}else{
			builder.append("\t\t\tif (").append(pk.getAtributeName(table.getName())).append(" == null || ").append(table.getObjectName())
				.append(".get").append(pk.getMethodSufix(table.getName())).append("() == null)\n");
			builder.append("\t\t\t\treturn false;\n");
			builder.append("\t\t\treturn ").append(pk.getAtributeName(table.getName()));
			builder.append(".compareTo(").append(table.getObjectName())
				.append(".get").append(pk.getMethodSufix(table.getName())).append("()) == 0;\n");
		}
		builder.append("\t\t}\n");
		builder.append("\t\treturn false;\n");
		builder.append("\t}\n");
		return builder.toString();
	}

	private String getClassContent(Table table){
		StringBuilder builder = new StringBuilder();
		//Nome do pacote
		builder.append("package model;\n\n");
		//Bibliotecas
		List<String> imports = getImports(table);
		for (String string : imports) {
			builder.append(string).append("\n");
		}
		//Nome da classe
		builder.append("\n").append("public class ").append(table.getClassName()).append(" extends Entity{\n\n");
		//Atributos
		for (Column column : table.getColumns()) {
			builder.append("\tprivate ");
			if (!column.isForeignKey())
				builder.append(column.getType().getJavaType().getSimpleName()).append(" ").append(column.getAtributeName(table.getName()));
			else
				builder.append(column.getForeignKey().getClassName()).append(" ").append(column.getForeignKey().getObjectName());
			builder.append(";\n");
		}
		//Listas
		if (table.getReferences().size() > 0){
			builder.append("\n");
			for (Reference ref : table.getReferences()) {
				if (ref.createList()){
					builder.append("\tprivate DataList<");
					builder.append(ref.get().getClassName());
					builder.append("> list");
					builder.append(ref.get().getClassName());
					builder.append(";\n");
				}
			}
		}
		//construtor padrao
		builder.append("\n\tpublic ").append(table.getClassName()).append("(){\n\t\tsuper();\n\t}\n\n");
		//Metodos
		for (Column column : table.getColumns()) {
			builder.append(getGetterMethod(column, table.getName())).append("\n");
			builder.append(getSetterMethod(column, table.getName())).append("\n");
		}
		for (Reference ref : table.getReferences()){
			if (ref.createList()){
				builder.append(getCheckMethod(ref, table)).append("\n");
				builder.append(getGetterMethod(ref)).append("\n");
				builder.append(getSetterMethod(ref)).append("\n");
				builder.append(getAddMethod(ref, table)).append("\n");
				builder.append(getSetMethod(ref, table)).append("\n");
				builder.append(getRemoveMethod(ref)).append("\n");
			}
		}
		//Equals
		builder.append(getEqualsMethod(table)).append("\n");
		//fim da classe
		builder.append("}\n");
		return builder.toString();
	}

	public List<FileData> export(){
		List<FileData> list = new LinkedList<>();
		for (Table table : database.getTables()) {
			if (!table.isNtoN()){
				FileData fd = new FileData(table.getFileName(), getClassContent(table));
				list.add(fd);
			}
		}
		return list;
	}

	public void print(String tableName){
		Table table = database.getTable(tableName);
		if (!table.isNtoN()){
			String content = getClassContent(table);
			System.out.println(content);
		}
	}

}
