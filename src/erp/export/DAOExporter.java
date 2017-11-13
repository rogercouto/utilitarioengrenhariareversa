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

public class DAOExporter {

	private Database database;

	public DAOExporter(Database database) {
		super();
		this.database = database;
	}

	private static List<Table> getDistinctFks(Table table){
		List<Table> dl = new LinkedList<>();
		for (Table t : table.getFks()) {
			boolean in = false;
			for (Table dt : dl) {
				if (t.getName().compareTo(dt.getName()) == 0){
					in = true;
					break;
				}
			}
			if (!in)
				dl.add(t);
		}
		for (Reference t : table.getReferences()) {
			boolean in = false;
			for (Table dt : dl) {
				if (t.get().getName().compareTo(dt.getName()) == 0){
					in = true;
					break;
				}
			}
			if (!in)
				dl.add(t.get());
		}
		return dl;
	}

	private List<String> getImports(Table table){
		List<String> list = new LinkedList<>();
		list.add("import java.sql.PreparedStatement;");
		list.add("import java.sql.ResultSet;");
		list.add("import java.sql.SQLException;\n");
		list.add("import model.".concat(table.getClassName()).concat(";"));
		for (Table fk : getDistinctFks(table)) {
			StringBuilder builder = new StringBuilder();
			builder.append("import model.").append(fk.getClassName()).append(";");
			list.add(builder.toString());
		}
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
			list.add("import java.sql.Date;");
		if (useTime)
			list.add("import java.sql.Time;");
		if (useDateTime)
			list.add("import java.sql.Timestamp;");
		list.add("\n");//filler
		list.add("import model.data.DataList;");
		if (table.importSession()){
			list.add("import model.data.Session;");
			list.add("import model.data.SessionManager;");
		}
		list.add("import model.data.ValidationException;");
		list.add("import model.data.DAO;");
		return list;
	}

	private String getInsertSql(Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("INSERT INTO ");
		builder.append(table.getName());
		builder.append("(");
		int count = 0;
		for (Column column : table.getColumns()) {
			if (column.isAutoIncrement())
				continue;
			if (count > 0)
				builder.append(",");
			builder.append(column.getName());
			count++;
		}
		builder.append(") VALUES (");
		for (int i = 0; i < count; i++){
			if (i > 0)
				builder.append(",");
			builder.append("?");
		}
		builder.append(")");
		return builder.toString();
	}

	private String getUpdateSql(Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("UPDATE ");
		builder.append(table.getName());
		builder.append(" SET ");
		int count = 0;
		List<String> pks = new LinkedList<String>();
		for (Column column : table.getColumns()) {
			if (column.isPrimaryKey())
				pks.add(column.getName());
			if (column.isAutoIncrement())
				continue;
			if (count > 0)
				builder.append(",");
			builder.append(column.getName());
			builder.append("=?");
			count++;
		}
		if (pks.size() == 0)
			throw new RuntimeException("Nenhum chave primária especificada para a tabela "+table.getName()+"!");
		builder.append(" WHERE ");
		for (String pk : pks) {
			if (!pk.equals(pks.get(0)))
				builder.append(" AND ");
			builder.append(pk);
			builder.append("=?");
		}
		return builder.toString();
	}

	private String getDeleteSql(Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("DELETE FROM ");
		builder.append(table.getName());
		List<String> pks = new LinkedList<String>();
		for (Column column : table.getColumns()) {
			if (column.isPrimaryKey())
				pks.add(column.getName());
		}
		if (pks.size() == 0)
			throw new RuntimeException("Nenhum chave primária especificada para a tabela "+table.getName()+"!");
		builder.append(" WHERE ");
		for (String pk : pks) {
			if (!pk.equals(pks.get(0)))
				builder.append(" AND ");
			builder.append(pk);
			builder.append("=?");
		}
		return builder.toString();
	}

	class IString{
		int index;
		String string;
		public IString(int index, String sql) {
			super();
			this.index = index;
			this.string = sql;
		}
		@Override
		public String toString(){
			return string;
		}
	}

	private IString getJoinSql(int n, Table table){
		StringBuilder builder = new StringBuilder();
		Integer tn = n;
		for (Column column : table.getColumns()) {
			if (column.isForeignKey()){
				if (column.getForeignKey().isNtoN())
					continue;//nao deixa juntar com tabelas com mais de uma chave primária
				builder.append("\"\n\t\t\t\t+\"");
				builder.append(" LEFT OUTER JOIN ");
				builder.append(column.getForeignKey().getName());
				builder.append(" t");
				builder.append(++n);
				builder.append(" ON t");
				builder.append(n);
				builder.append('.');
				builder.append(column.getForeignKey().getPrimaryKey().getName());
				builder.append(" = t");
				builder.append(tn);
				builder.append('.');
				builder.append(column.getName());
				IString jSql = getJoinSql(n, column.getForeignKey());
				n = jSql.index;
				builder.append(jSql.toString());
			}
		}
		return new IString(n, builder.toString());
	}

	private String getSelectSql(Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("SELECT * FROM ");
		builder.append(table.getName());
		builder.append(" t1");
		builder.append(getJoinSql(1, table).toString());
		return builder.toString();
	}

	private String getInitMethod(Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("\t@Override\n\tprotected void initialize() {\n");
		builder.append("\t\tinsertSql = \"");
		builder.append(getInsertSql(table));
		builder.append("\";\n");
		builder.append("\t\tupdateSql = \"");
		builder.append(getUpdateSql(table));
		builder.append("\";\n");
		builder.append("\t\tdeleteSql = \"");
		builder.append(getDeleteSql(table));
		builder.append("\";\n");
		builder.append("\t\tselectSql = \"");
		builder.append(getSelectSql(table));
		builder.append("\";\n");
		builder.append("\t\tgetFilter = \"");
		for (Column key : table.getPrimaryKeys()) {
			if (!key.equals(table.getPrimaryKeys().get(0)))
				builder.append(" AND ");
			builder.append(key.getName());
			builder.append(" = ?");
		}
		builder.append("\";\n");
		for (Column column : table.getColumns()) {
			if (column.getType().getJavaType().equals(String.class))
				builder.append("\t\tfindFilters.add(\"UPPER(t1.").append(column.getName()).append(") LIKE ?\");\n");
		}
		builder.append("\t}\n\n");
		return builder.toString();
	}

	private String getColumnsFill(int num, Table table){
		StringBuilder builder = new StringBuilder();
		for (Column column : table.getColumns()) {
			if (column.isAutoIncrement())
				continue;
			builder.append("\t\tps.set");
			builder.append(column.getType().getJavaSQLSufix());
			builder.append("(");
			builder.append(num++);
			builder.append(',');
			if (!column.isForeignKey()){
				//TODO
				if (column.getType().getJavaType().equals(LocalDate.class)
				||column.getType().getJavaType().equals(LocalTime.class)
				||column.getType().getJavaType().equals(LocalDateTime.class))
				{
					builder.append(table.getObjectName());
					builder.append('.');
					if (column.getType().getJavaType().equals(Boolean.class)||column.getType().getJavaType().equals(Boolean.TYPE))
						builder.append("is");
					else
						builder.append("get");
					builder.append(column.getMethodSufix(table.getName()));
					builder.append("() != null ?\n\t\t\t");
					if (column.getType().getJavaType().equals(LocalDate.class))
						builder.append("Date.valueOf(");
					else if (column.getType().getJavaType().equals(LocalTime.class))
						builder.append("Time.valueOf(");
					else if (column.getType().getJavaType().equals(LocalDateTime.class))
						builder.append("Timestamp.valueOf(");
				}
				builder.append(table.getObjectName());
				builder.append('.');
				if (column.getType().getJavaType().equals(Boolean.class)||column.getType().getJavaType().equals(Boolean.TYPE))
					builder.append("is");
				else
					builder.append("get");
				builder.append(column.getMethodSufix(table.getName()));
				builder.append("()");
				if (column.getType().getJavaType().equals(LocalDate.class)
				|| column.getType().getJavaType().equals(LocalTime.class)
				|| column.getType().getJavaType().equals(LocalDateTime.class))
					builder.append(") : null");
				builder.append(");\n");
			}else{
				Table ref = column.getForeignKey();
				Column pk = column.getForeignKey().getPrimaryKey();
				if (column.isNullable()){
					builder.append(table.getObjectName());
					builder.append(".get");
					builder.append(ref.getClassName());
					builder.append("() != null ? ");
				}
				builder.append(table.getObjectName());
				builder.append('.');
				builder.append("get");
				builder.append(ref.getClassName());
				builder.append("().get");
				builder.append(pk.getMethodSufix(table.getName()));
				builder.append("()");
				if (column.isNullable())
					builder.append(" : null");
				builder.append(");\n");
			}
		}
		builder.append("\t\treturn ");
		builder.append(num);
		builder.append(";\n");
		return builder.toString();
	}

	private String getFillObjectMethod(Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("\t@Override\n\tprotected int fillObject(PreparedStatement ps, ");
		builder.append(table.getClassName()+" "+table.getObjectName());
		builder.append(") throws SQLException {\n");
		builder.append(getColumnsFill(1, table));
		builder.append("\t}\n");
		return builder.toString();
	}

	private String getPKFill(Table table){
		StringBuilder builder = new StringBuilder();
		List<Column> pks = table.getPrimaryKeys();
		for (Column column : pks) {
			if (!column.equals(pks.get(0)))
				builder.append("\t\t");
			builder.append("ps.set");
			builder.append(column.getType().getJavaSQLSufix());
			builder.append("(parameterIndex++, ");
			builder.append(table.getObjectName());
			builder.append('.');
			builder.append("get");
			builder.append(column.getMethodSufix(table.getName()));
			builder.append("());\n");
		}
		return builder.toString();
	}

	private String getFillPrimaryKey(Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("\t@Override\n\tprotected void fillPrimaryKey(int parameterIndex, PreparedStatement ps, ");
		builder.append(table.getClassName()+" "+table.getObjectName());
		builder.append(") throws SQLException {\n");
		builder.append("\t\t");
		builder.append(getPKFill(table));
		builder.append("\t}\n");
		return builder.toString();
	}

	private String getBeforeSave(Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("\t\tString nullError = \"Dado(s) incompleto(s)!\";\n");
		int count = 0;
		for (Column column : table.getColumns()) {
			if (!column.isNullable() && !column.isAutoIncrement()){
				if (column.getType().isPrimitive())
					continue;
				builder.append("\t\tif (").append(table.getObjectName());
				if (column.getType().getJavaType().equals(Boolean.class))
					builder.append(".is");
				else
					builder.append(".get");
				if (!column.isForeignKey())
					builder.append(column.getMethodSufix(table.getName()));
				else
					builder.append(column.getForeignKey().getClassName());
				builder.append("() == null)\n");
				builder.append("\t\t\tthrow new ValidationException(nullError);\n");
				count++;
			}
		}
		if (count == 0)
			return "";
		return builder.toString();
	}

	private String getVerifyUnique(boolean insert, Table table){
		StringBuilder builder = new StringBuilder();
		for (Column column : table.getColumns()) {
			if (column.isUnique()){
				builder.append("\t\tString uniqueError = \"Dado(s) j\u00e1 cadastrado(s)!\";\n");
				if (insert){
					builder.append("\t\tif (findList(\"t1.").append(column.getName()).append(" = ?\", ")
					.append(table.getObjectName()).append(".get").append(column.getMethodSufix(table.getName())).append("()).size() > 0)\n");
				}else{
					Column pk = table.getPrimaryKey();
					builder.append("\t\tif (findList(\"t1.").append(column.getName())
					.append(" = ? AND t1.").append(pk.getName()).append(" <> ?\", new Object[]{")
					.append(table.getObjectName()).append(".get").append(column.getMethodSufix(table.getName()))
					.append("(), ").append(table.getObjectName()).append(".get")
					.append(pk.getMethodSufix(table.getName())).append("()}).size() > 0)\n");
				}
				builder.append("\t\t\tthrow new ValidationException(uniqueError);\n");
			}
		}
		return builder.toString();
	}

	private String getVerifyDelete(Table table){
		StringBuilder builder = new StringBuilder();
		if (table.getReferences().size() > 0){
			builder.append("\t\tString deleteError = \"Imposs\u00edvel excluir, dado(s) em uso!\";\n");
			for (Reference ref : table.getReferences()) {
				builder.append("\t\tif (").append(table.getObjectName()).append(".getList").append(ref.get().getClassName()).append("().size() > 0)\n");
				builder.append("\t\t\tthrow new ValidationException(deleteError);\n");
			}
		}
		return builder.toString();
	}

	private String getBeforeInsert(Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("\t@Override\n\tprotected void beforeInsert(");
		builder.append(table.getClassName()+" "+table.getObjectName());
		builder.append(") throws ValidationException {\n");
		builder.append(getBeforeSave(table));
		builder.append(getVerifyUnique(true, table));
		builder.append("\t}\n");
		return builder.toString();
	}

	private String getKeyGen(Table table){
		StringBuilder builder = new StringBuilder();
		Column column = table.getPrimaryKey();
		if (column == null)
			throw new RuntimeException("Table "+table.getName()+" without primary key!");
		if (column.isAutoIncrement()){
			builder.append("\t\t");
			builder.append(table.getObjectName());
			builder.append(".set");
			builder.append(column.getMethodSufix(table.getName()));
			builder.append("(keyGen);\n");
		}
		return builder.toString();
	}

	private String getAfterInsert(Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("\t@Override\n\tprotected void afterInsert(int keyGen, ");
		builder.append(table.getClassName());
		builder.append(" ");
		builder.append(table.getObjectName());
		builder.append(") throws ValidationException, SQLException {\n");
		builder.append(getKeyGen(table));
		if (table.getNtoNRefCount() > 0)
			builder.append("\t\tSession session = SessionManager.createSession();\n");
		for (Reference ref : table.getReferences()) {
			if (!ref.isNtoN()){
				builder.append("\t\tif(");
				builder.append(table.getObjectName());
				builder.append(".getList");
				builder.append(ref.get().getClassName());
				builder.append("().isModifyed()) {\n");
				builder.append("\t\t\t");
				builder.append(ref.get().getClassName());
				builder.append("DAO ");
				builder.append(ref.get().getObjectName());
				builder.append("DAO  = new ");
				builder.append(ref.get().getClassName());
				builder.append("DAO();\n");
				builder.append("\t\t\tfor (");
				builder.append(ref.get().getClassName());
				builder.append(" ");
				builder.append(ref.get().getObjectName());
				builder.append(" : ");
				builder.append(ref.get().getObjectName());
				builder.append("DAO.getList())\n\t\t\t\t");
				builder.append(ref.get().getObjectName());
				builder.append("DAO.insert(");
				builder.append(ref.get().getObjectName());
				builder.append(");\n\t\t}\n");
			}else{
				builder.append("\t\tif(");
				builder.append(table.getObjectName());
				builder.append(".getList");
				builder.append(ref.get().getClassName());
				builder.append("().isModifyed()) {\n");
				builder.append("\t\t\tfor (");
				builder.append(ref.get().getClassName());
				builder.append(" ");
				builder.append(ref.get().getObjectName());
				builder.append(" : ");
				builder.append(table.getObjectName());
				builder.append(".getList");
				builder.append(ref.get().getClassName());
				builder.append("()){\n");
				builder.append("\t\t\t\tString sql = \"INSERT INTO ");
				builder.append(ref.getTable().getName());
				builder.append(" VALUES (");
				for (int i = 0; i < ref.getTable().getColumns().size(); i++) {
					if (i > 0)
						builder.append(", ");
					builder.append("?");
				}
				builder.append(")\";\n");
				builder.append("\t\t\t\tPreparedStatement ps = session.prepareStatement(sql);\n");
				int pi = 1;
				for (Column column : ref.getTable().getColumns()) {
					builder.append("\t\t\t\tps.set");
					builder.append(column.getType().getJavaSQLSufix());
					builder.append("(");
					builder.append(pi++);
					builder.append(", ");
					if (column.getName().compareTo(table.getPrimaryKey().getName()) == 0){
						builder.append(table.getObjectName());
						builder.append(".get");
						builder.append(table.getPrimaryKey().getMethodSufix(table.getName()));
						builder.append("())");
					}else if(column.getName().compareTo(ref.get().getPrimaryKey().getName()) == 0){
						builder.append(ref.get().getObjectName());
						builder.append(".get");
						builder.append(ref.get().getPrimaryKey().getMethodSufix(table.getName()));
						builder.append("())");
					}
					builder.append(";\n");
				}
				builder.append("\t\t\t\tps.executeUpdate();\n\t\t\t}\n");
				builder.append("\t\t}\n");
			}
		}
		builder.append("\t}\n");
		return builder.toString();
	}

	private String getVerifyPk(Table table){
		StringBuilder builder = new StringBuilder();
		Column column = table.getPrimaryKey();
		if (column.getType().isPrimitive())
			return "";
		builder.append("\t\tif (").append(table.getObjectName()).append(".get").append(column.getMethodSufix(table.getName())).append("() == null)\n");
		builder.append("\t\t\tthrow new RuntimeException(\"Primary key can't be null!\");\n");
		return builder.toString();
	}

	private String getBeforeUpdate(Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("\t@Override\n\tprotected void beforeUpdate(");
		builder.append(table.getClassName()+" "+table.getObjectName());
		builder.append(") throws ValidationException {\n");
		builder.append(getVerifyPk(table));
		builder.append(getBeforeSave(table));
		builder.append(getVerifyUnique(false, table));
		builder.append("\t}\n");
		return builder.toString();
	}

	private String getAfterUpdate(Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("\t@Override\n\tprotected void afterUpdate(");
		builder.append(table.getClassName()+" "+table.getObjectName());
		builder.append(") throws ValidationException, SQLException{\n");
		if (table.getReferences().size() > 0)
			builder.append("\t\tSession session = SessionManager.createSession();\n");
		for (Reference ref : table.getReferences()) {
			if (!ref.isNtoN()){
				builder.append("\t\tif(");
				builder.append(table.getObjectName());
				builder.append(".getList");
				builder.append(ref.get().getClassName());
				builder.append("().isModifyed()) {\n");
				builder.append("\t\t\tString sql = \"DELETE FROM ");
				builder.append(ref.get().getName());
				builder.append(" WHERE ");
				builder.append(table.getPrimaryKey().getName());
				builder.append(" = ?\";\n");
				builder.append("\t\t\tPreparedStatement ps = session.prepareStatement(sql);\n");
				builder.append("\t\t\tps.set");
				builder.append(table.getPrimaryKey().getType().getJavaSQLSufix());
				builder.append("(1, ");
				builder.append(table.getObjectName());
				builder.append(".get");
				builder.append(table.getPrimaryKey().getMethodSufix(table.getName()));
				builder.append("());\n");
				builder.append("\t\t\tps.executeUpdate();\n");
				builder.append("\t\t\t");
				builder.append(ref.get().getClassName());
				builder.append("DAO ");
				builder.append(ref.get().getObjectName());
				builder.append("DAO  = new ");
				builder.append(ref.get().getClassName());
				builder.append("DAO();\n");
				builder.append("\t\t\tfor (");
				builder.append(ref.get().getClassName());
				builder.append(" ");
				builder.append(ref.get().getObjectName());
				builder.append(" : ");
				builder.append(table.getObjectName());
				builder.append(".getList");
				builder.append(ref.get().getClassName());
				builder.append("())\n\t\t\t\t");
				builder.append(ref.get().getObjectName());
				builder.append("DAO.insert(");
				builder.append(ref.get().getObjectName());
				builder.append(");\n\t\t}\n");
			}else{
				builder.append("\t\tif(");
				builder.append(table.getObjectName());
				builder.append(".getList");
				builder.append(ref.get().getClassName());
				builder.append("().isModifyed()) {\n");
				builder.append("\t\t\tString sql = \"DELETE FROM ");
				builder.append(ref.getTable().getName());
				builder.append(" WHERE ");
				builder.append(table.getPrimaryKey().getName());
				builder.append(" = ?\";\n");
				builder.append("\t\t\tPreparedStatement ps = session.prepareStatement(sql);\n");
				builder.append("\t\t\tps.set");
				builder.append(table.getPrimaryKey().getType().getJavaSQLSufix());
				builder.append("(1, ");
				builder.append(table.getObjectName());
				builder.append(".get");
				builder.append(table.getPrimaryKey().getMethodSufix(table.getName()));
				builder.append("());\n");
				builder.append("\t\t\tps.executeUpdate();\n");
				builder.append("\t\t\tfor (");
				builder.append(ref.get().getClassName());
				builder.append(" ");
				builder.append(ref.get().getObjectName());
				builder.append(" : ");
				builder.append(table.getObjectName());
				builder.append(".getList");
				builder.append(ref.get().getClassName());
				builder.append("()){\n");
				builder.append("\t\t\t\tsql = \"INSERT INTO ");
				builder.append(ref.getTable().getName());
				builder.append(" VALUES (");
				for (int i = 0; i < (ref.getTable().getColumns().size()); i++) {
					if (i > 0)
						builder.append(", ");
					builder.append("?");
				}
				builder.append(")\";\n");
				builder.append("\t\t\t\tps = session.prepareStatement(sql);\n");
				int pi = 1;
				for (Column column : ref.getTable().getColumns()) {
					builder.append("\t\t\t\tps.set");
					builder.append(column.getType().getJavaSQLSufix());
					builder.append("(");
					builder.append(pi++);
					builder.append(", ");
					if (column.getName().compareTo(table.getPrimaryKey().getName()) == 0){
						builder.append(table.getObjectName());
						builder.append(".get");
						builder.append(table.getPrimaryKey().getMethodSufix(table.getName()));
						builder.append("())");
					}else if(column.getName().compareTo(ref.get().getPrimaryKey().getName()) == 0){
						builder.append(ref.get().getObjectName());
						builder.append(".get");
						builder.append(ref.get().getPrimaryKey().getMethodSufix(table.getName()));
						builder.append("())");
					}
					builder.append(";\n");
				}
				builder.append("\t\t\t\tps.executeUpdate();\n");
				builder.append("\t\t\t}\n\t\t}\n");
			}
		}
		builder.append("\t}\n");
		return builder.toString();
	}

	private String getBeforeDelete(Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("\t@Override\n\tprotected void beforeDelete(");
		builder.append(table.getClassName()+" "+table.getObjectName());
		builder.append(") throws ValidationException {\n");
		builder.append(getVerifyPk(table));
		builder.append(getVerifyDelete(table));
		builder.append("\t}\n");
		return builder.toString();
	}

	private IString getFillObject(int tabs, IString is, Table table){
		StringBuilder builder = new StringBuilder();
		int tn = is.index;
		for (Column column : table.getColumns()) {
			if (!column.isForeignKey()){
				String sqlGet = "get".concat(column.getType().getJavaSQLSufix());
				for (int i = 0; i < tabs; i++)
					builder.append("\t");
				builder.append("\t\t");
				builder.append(table.getObjectName());
				builder.append(".set");
				builder.append(column.getMethodSufix(table.getName()));
				builder.append("(");
				if (sqlGet.compareTo("getObject") == 0){
					builder.append("(");
					builder.append(column.getType().getJavaType().getSimpleName());
					builder.append(") ");
				}
				builder.append("result.");
				builder.append(sqlGet);
				builder.append("(\"t");
				builder.append(tn);
				builder.append('.');
				builder.append(column.getName());
				builder.append("\")");
				if (column.getType().getJavaType().equals(LocalDate.class)
				||column.getType().getJavaType().equals(LocalTime.class)
				||column.getType().getJavaType().equals(LocalDateTime.class)
				){
					builder.append(" != null ?\n\t\t\t\tresult.").append(sqlGet).append("(\"t").append(tn).append('.').append(column.getName()).append("\")");
					if (column.getType().getJavaType().equals(LocalDate.class))
						builder.append(".toLocalDate()");
					else if (column.getType().getJavaType().equals(LocalTime.class))
						builder.append(".toLocalTime()");
					else if (column.getType().getJavaType().equals(LocalDateTime.class))
						builder.append(".toLocalDateTime()");
					builder.append(" : null ");
				}
				builder.append(");\n");
			}else{
				if (!column.getForeignKey().isNtoN()){
					if (column.isNullable()){
						for (int i = 0; i < tabs+2; i++)
							builder.append("\t");
						builder.append("if (result.getObject(\"t");
						builder.append(tn);
						builder.append(".");
						builder.append(column.getName());
						builder.append("\") != null){\n");
					}
					for (int i = 0; i < tabs+2; i++)
						builder.append("\t");
					if (column.isNullable())
						builder.append("\t");
					builder.append(column.getForeignKey().getClassName());
					builder.append(' ');
					builder.append(column.getForeignKey().getObjectName());
					builder.append(" = new ");
					builder.append(column.getForeignKey().getClassName());
					builder.append("();\n");
					IString nis = getFillObject(column.isNullable()?(tabs+1):tabs, new IString(is.index+1, null), column.getForeignKey());
					is.index  = nis.index;
					builder.append(nis.string);
					for (int i = 0; i < tabs+2; i++)
						builder.append("\t");
					if (column.isNullable())
						builder.append("\t");
					builder.append(table.getObjectName());
					builder.append(".set");
					builder.append(column.getForeignKey().getClassName());
					builder.append("(");
					builder.append(column.getForeignKey().getObjectName());
					builder.append(");\n");
					if (column.isNullable()){
						for (int i = 0; i < tabs; i++)
							builder.append("\t");
						builder.append("\t\t}\n");
					}
				}
			}
		}
		is.string = builder.toString();
		return is;
	}

	private String getGetEntity(Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("\t@Override\n\tprotected ");
		builder.append(table.getClassName());
		builder.append(" getEntity(ResultSet result) throws SQLException {\n\t\t");
		builder.append(table.getClassName());
		builder.append(' ');
		builder.append(table.getObjectName());
		builder.append(" = new ");
		builder.append(table.getClassName());
		builder.append("();\n");
		builder.append(getFillObject(0, new IString(1, null), table));
		builder.append("\t\treturn ");
		builder.append(table.getObjectName());
		builder.append(";\n\t}\n");
		return builder.toString();
	}

	private String getFillSearch(Table table){
		StringBuilder builder = new StringBuilder();
		builder.append("\t@Override\n\tprotected void fillSearch(PreparedStatement ps, String text) throws SQLException {\n");
		int nc = 1;
		for (Column column : table.getColumns()) {
			if (column.getType().getJavaType().equals(String.class))
				builder.append("\t\tps.setString(").append(nc++).append(", \"%\"+text.toUpperCase()+\"%\");\n");
		}
		builder.append("\t}\n");
		return builder.toString();
	}

	private int getTNext(Table table){
		return table.getFks().size()+2;
	}

	private String getFillExtra(Table table){
		StringBuilder builder = new StringBuilder();
		for (Column column : table.getColumns()) {
			if (column.isForeignKey()){
				builder.append("\tpublic DataList<").append(table.getClassName())
					.append("> getList(")
					.append(column.getForeignKey().getClassName()).append(" ").append(column.getForeignKey().getObjectName()).append("){\n");
				builder.append("\t\tString filter = \" WHERE t1.").append(column.getForeignKey().getPrimaryKey().getName()).append(" = ?\";\n");
				builder.append("\t\treturn findList(filter, ").append(column.getForeignKey().getObjectName())
					.append(".get").append(column.getForeignKey().getPrimaryKey().getMethodSufix(table.getName())).append("());\n");
				builder.append("\t}\n\n");
			}
		}
		int tNext = getTNext(table);
		for (Reference ref : table.getReferences()) {
			builder.append("\tpublic DataList<").append(table.getClassName())
				.append("> getList(")
				.append(ref.get().getClassName()).append(" ").append(ref.get().getObjectName()).append("){\n");
			builder.append("\t\tString filter = \" LEFT OUTER JOIN ").append(ref.getTable().getName())
				.append(" t").append(tNext).append(" ON ").append("t1.").append(table.getPrimaryKey().getName())
				.append(" = t").append(tNext).append(".").append(table.getPrimaryKey().getName()).append(" WHERE ")
				.append(ref.get().getPrimaryKey().getName()).append(" = ?\";\n");
			builder.append("\t\treturn findList(filter, ").append(ref.get().getObjectName())
				.append(".get").append(ref.get().getPrimaryKey().getMethodSufix(table.getName())).append("());\n");
			builder.append("\t}\n\n");
		}
		return builder.toString();
	}

	private String getClassContent(Table table){
		StringBuilder builder = new StringBuilder();
		//Nome do pacote
		builder.append("package model.dao;\n\n");
		//Bibliotecas
		for (String string : getImports(table)) {
			builder.append(string).append("\n");
		}
		//nome da classe
		builder.append("\npublic class ").append(table.getClassName()).append("DAO extends DAO<")
			.append(table.getClassName()).append(">{\n\n");
		//Initialize
		builder.append(getInitMethod(table)).append("\n");
		//FillObject
		builder.append(getFillObjectMethod(table)).append("\n");
		//FillPrimaryKey
		builder.append(getFillPrimaryKey(table)).append("\n");
		//BeforeInsert
		builder.append(getBeforeInsert(table)).append("\n");
		//AfterInsert
		builder.append(getAfterInsert(table)).append("\n");
		//BeforeUpdate
		builder.append(getBeforeUpdate(table)).append("\n");
		//AfterUpdate
		builder.append(getAfterUpdate(table)).append("\n");
		//BeforeDelete
		builder.append(getBeforeDelete(table)).append("\n");
		//GetEntity
		builder.append(getGetEntity(table)).append("\n");
		//FillSearch
		builder.append(getFillSearch(table)).append("\n");
		//Extra
		builder.append(getFillExtra(table));
		//Fim da classe
		builder.append("}");
		return builder.toString();
	}

	public List<FileData> export(){
		List<FileData> list = new LinkedList<>();
		for (Table table : database.getTables()) {
			if (!table.isNtoN()){
				FileData fd = new FileData(table.getDAOFileName(), getClassContent(table));
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
