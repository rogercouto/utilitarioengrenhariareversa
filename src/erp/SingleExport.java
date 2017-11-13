package erp;

import erp.db.MysqlServer;
import erp.export.ModelExporter;
import erp.model.DatabaseFactory;

public class SingleExport {

	public static void main(String[] args) {
		String table = "usuario";
		MysqlServer server = new MysqlServer();
		server.setProperties("jdbc:mysql://localhost:3306/biblioteca", "root", "");
		DatabaseFactory factory = new DatabaseFactory("biblioteca", server);
		/*
		DAOExporter de = new DAOExporter(factory.getDatabase());
		de.print(table);
		*/
		ModelExporter me = new ModelExporter(factory.getDatabase());
		me.print(table);
	}

}
