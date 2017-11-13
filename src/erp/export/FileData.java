package erp.export;

public class FileData {

	private String fileName;
	private String fileContent;
	
	public FileData(String fileName, String fileContent) {
		super();
		this.fileName = fileName;
		this.fileContent = fileContent;
	}
	
	public String getFileName() {
		return fileName;
	}
	public String getFileContent() {
		return fileContent;
	}
	
}
