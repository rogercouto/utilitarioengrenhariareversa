package erp.model;

public class Reference {

	private Table table;
	private Table destiny = null;
	private boolean createList = true;
	
	public Reference(Table table) {
		super();
		this.table = table;
	}
	public Reference(Table table, Table destiny) {
		super();
		this.table = table;
		this.destiny = destiny;
	}
	public Table getTable() {
		return table;
	}
	public void setTable(Table table) {
		this.table = table;
	}
	public Table getDestiny() {
		return destiny;
	}
	public void setDestiny(Table destiny) {
		this.destiny = destiny;
	}
	
	public boolean createList() {
		return createList;
	}
	public void setCreateList(boolean createList) {
		this.createList = createList;
	}
	public Table get(){
		return (destiny != null) ? destiny : table;
	}
	public boolean isNtoN(){
		return destiny != null;
	}
}
