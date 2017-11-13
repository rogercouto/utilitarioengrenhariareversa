package erp.model;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.List;

import erp.db.Server;

/**
 * Classe responsável por coletar as informações sobre banco de dados
 * @author Roger
 */
public class DatabaseFactory {

	private Server server;
	private Database database;
	
	public DatabaseFactory(String databaseName, Server server) {
		super();
		database = new Database(databaseName);
		this.server = server;
		try {
			initialize();
		} catch (SQLException e) {
			e.printStackTrace();
			throw new RuntimeException(e.getMessage(), e.getCause());
		}
	}
	
	private void initialize() throws SQLException{
		Connection connection = server.getConnection();
		DatabaseMetaData md = connection.getMetaData();
		ResultSet result = md.getTables(null, null, null, null);
		ResultSetMetaData rmd = result.getMetaData();
		while (result.next()){
			for (int i = 1; i <= rmd.getColumnCount(); i++) {
				if (rmd.getColumnName(i).compareTo("TABLE_NAME") == 0)
					database.addTable(new Table(result.getString(i)));
			}
		}
		result.close();
		for (Table table : database.getTables()) {
			result = md.getColumns(null, null, table.getName(), null);
			rmd = result.getMetaData();
			while (result.next()){
				Column column = new Column();
				String typeName = null;
				for (int i = 1; i <= rmd.getColumnCount(); i++) {
					if (rmd.getColumnName(i).compareTo("COLUMN_NAME") == 0)
						column.setName(result.getString(i));
					if (rmd.getColumnName(i).compareTo("TYPE_NAME") == 0)
						typeName = result.getString(i);
					if (rmd.getColumnName(i).compareTo("COLUMN_SIZE") == 0)
						column.setSize(result.getInt(i));
					if (rmd.getColumnName(i).compareTo("NULLABLE") == 0)
						column.setNullable(result.getInt(i) == 1);
					if (rmd.getColumnName(i).compareTo("IS_AUTOINCREMENT") == 0 && result.getString(i).compareTo("YES") == 0)
						column.setAutoIncrement(true);
				}
				column.setType(server.getType(typeName, column.getSize()));
				table.addColumn(column);
			}
			result.close();
			result = md.getIndexInfo(null, null, table.getName(), false, false);
			rmd = result.getMetaData();
			while (result.next()){
				String columnName = null;
				boolean pk = false;
				boolean un = false;
				for (int i = 1; i <= rmd.getColumnCount(); i++) {
					if (rmd.getColumnName(i).compareTo("COLUMN_NAME") == 0)
						columnName = result.getString(i);
					else if (rmd.getColumnName(i).compareTo("INDEX_NAME") == 0){
						String indexName = result.getString(i);
						if (indexName.compareTo("PRIMARY") == 0)
							pk = true;
						else if (indexName.contains("_UNIQUE"))
							un = true;
					}
				}
				if (columnName != null){
					if (pk)
						table.getColumn(columnName).setPrimaryKey(true);
					if (un)
						table.getColumn(columnName).setUnique(true);
				}
			}
			result.close();
		}
		for (Table table : database.getTables()) {
			List<Table> list = database.getOtherTables(table);
			for (Table ot : list) {
				result = md.getCrossReference(null, null, ot.getName(), null, null, table.getName());
				rmd = result.getMetaData();
				while (result.next()){
					String pkColumnName = null;
					String pkTableName = null;
					for (int i = 1; i <= rmd.getColumnCount(); i++) {
						if (rmd.getColumnName(i).compareTo("PKCOLUMN_NAME") == 0)
							pkColumnName = result.getString(i);
						else if (rmd.getColumnName(i).compareTo("PKTABLE_NAME") == 0)
							pkTableName = result.getString(i);
					}
					if (pkColumnName != null && pkTableName != null){
						for (Table at : database.getTables()) {
							if (at.getName().compareTo(pkTableName) == 0){
								Column pk = table.getColumn(pkColumnName);
								if (pk != null)
									pk.setForeignKey(at);
								at.addReference(new Reference(table));
							}
						}
					}
				}
				result.close();
			}
		}
		for (Table table : database.getTables()) {
			if (!table.isNtoN()){
				for (Reference ref : table.getReferences()) {
					if (ref.getTable().isNtoN()){
						for (Column column : ref.getTable().getColumns()) {
							if (column.getForeignKey().getName().compareTo(table.getName()) != 0){
								ref.setDestiny(column.getForeignKey());
							}
						}
					}
				}
			}
		}
		connection.close();
	}
	
	/**
	 * @return Retorna o banco de dados criado
	 */
	public Database getDatabase(){
		return database;
	}

}
