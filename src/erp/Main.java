package erp;

import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.channels.FileChannel;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import erp.db.ConnectionFactory;
import erp.db.MysqlServer;
import erp.db.Server;
import erp.export.DAOExporter;
import erp.export.FileData;
import erp.export.ModelExporter;
import erp.model.Database;
import erp.model.DatabaseFactory;

public class Main {

	private JFrame frmUtilitrioDeEngenharia;
	private JTextField txtUrl;
	private JTextField txtBanco;
	private JTextField txtUsuario;
	private JPasswordField txtSenha;
	private JTextField txtPasta;
	private File dir;
	private JButton btnConcluir;
	private JCheckBox chckbxLibraries;
	private JCheckBox chckbxDaos;
	private JCheckBox chckbxModel;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e1) {
			e1.printStackTrace();
		}
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					Main window = new Main();
					window.frmUtilitrioDeEngenharia.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Main() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frmUtilitrioDeEngenharia = new JFrame();
		frmUtilitrioDeEngenharia.setTitle("Utilitário de Engenharia Reversa");
		frmUtilitrioDeEngenharia.setResizable(false);
		frmUtilitrioDeEngenharia.setBounds(100, 100, 450, 358);
		frmUtilitrioDeEngenharia.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frmUtilitrioDeEngenharia.getContentPane().setLayout(null);
		JLabel lblServidor = new JLabel("Servidor:");
		lblServidor.setHorizontalAlignment(SwingConstants.RIGHT);
		lblServidor.setBounds(25, 19, 92, 30);
		frmUtilitrioDeEngenharia.getContentPane().add(lblServidor);
		JComboBox<String> comboBox = new JComboBox<String>();
		comboBox.setFont(new Font("Dialog", Font.BOLD, 11));
		comboBox.setEnabled(false);
		comboBox.addItem("Mysql");
		comboBox.setBounds(127, 19, 270, 30);
		frmUtilitrioDeEngenharia.getContentPane().add(comboBox);
		JLabel lblUrl = new JLabel("Url:");
		lblUrl.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUrl.setBounds(25, 55, 92, 25);
		frmUtilitrioDeEngenharia.getContentPane().add(lblUrl);
		DocumentListener listener = new DocumentListener() {
			@Override
			public void changedUpdate(DocumentEvent e) {

			}
			@Override
			public void insertUpdate(DocumentEvent e) {
				verify();
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				verify();
			}
		};
		txtUrl = new JTextField();
		txtUrl.setFont(new Font("Dialog", Font.PLAIN, 11));
		txtUrl.getDocument().addDocumentListener(listener);
		txtUrl.setText("jdbc:mysql://localhost:3306/");
		txtUrl.setBounds(127, 52, 270, 35);
		frmUtilitrioDeEngenharia.getContentPane().add(txtUrl);
		txtUrl.setColumns(10);
		JLabel lblNewLabel = new JLabel("Banco:");
		lblNewLabel.setHorizontalAlignment(SwingConstants.RIGHT);
		lblNewLabel.setBounds(35, 90, 83, 24);
		frmUtilitrioDeEngenharia.getContentPane().add(lblNewLabel);
		txtBanco = new JTextField();
		txtBanco.setFont(new Font("Dialog", Font.PLAIN, 11));
		txtBanco.getDocument().addDocumentListener(listener);
		txtBanco.setBounds(127, 89, 270, 35);
		frmUtilitrioDeEngenharia.getContentPane().add(txtBanco);
		txtBanco.setColumns(10);
		JLabel lblUsurio = new JLabel("Usuário:");
		lblUsurio.setHorizontalAlignment(SwingConstants.RIGHT);
		lblUsurio.setBounds(44, 130, 73, 22);
		frmUtilitrioDeEngenharia.getContentPane().add(lblUsurio);
		JLabel lblSenha = new JLabel("Senha:");
		lblSenha.setHorizontalAlignment(SwingConstants.RIGHT);
		lblSenha.setBounds(54, 165, 65, 22);
		frmUtilitrioDeEngenharia.getContentPane().add(lblSenha);
		txtUsuario = new JTextField();
		txtUsuario.setFont(new Font("Dialog", Font.PLAIN, 11));
		txtUsuario.getDocument().addDocumentListener(listener);
		txtUsuario.setBounds(127, 125, 126, 35);
		frmUtilitrioDeEngenharia.getContentPane().add(txtUsuario);
		txtUsuario.setColumns(10);
		txtSenha = new JPasswordField();
		txtSenha.setFont(new Font("Dialog", Font.PLAIN, 11));
		txtSenha.getDocument().addDocumentListener(listener);
		txtSenha.setBounds(127, 162, 126, 35);
		frmUtilitrioDeEngenharia.getContentPane().add(txtSenha);
		JLabel lblPastaDoProjeto = new JLabel("Pasta src:");
		lblPastaDoProjeto.setHorizontalAlignment(SwingConstants.RIGHT);
		lblPastaDoProjeto.setBounds(25, 200, 93, 21);
		frmUtilitrioDeEngenharia.getContentPane().add(lblPastaDoProjeto);
		txtPasta = new JTextField();
		txtPasta.setFont(new Font("Dialog", Font.PLAIN, 11));
		txtPasta.setEditable(false);
		txtPasta.setBounds(127, 197, 217, 35);
		frmUtilitrioDeEngenharia.getContentPane().add(txtPasta);
		txtPasta.setColumns(10);

		JButton btnBusca = new JButton("...");
		btnBusca.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				buscaPasta();
			}
		});
		btnBusca.setBounds(343, 197, 46, 35);
		frmUtilitrioDeEngenharia.getContentPane().add(btnBusca);
		btnConcluir = new JButton("Concluir");
		btnConcluir.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				try {
					export();
				} catch (IOException e) {
					JOptionPane.showMessageDialog(null, e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		btnConcluir.setEnabled(false);
		btnConcluir.setBounds(318, 288, 104, 30);
		frmUtilitrioDeEngenharia.getContentPane().add(btnConcluir);
		chckbxModel = new JCheckBox("Entities");
		chckbxModel.setSelected(true);
		chckbxModel.setBounds(127, 230, 73, 23);
		frmUtilitrioDeEngenharia.getContentPane().add(chckbxModel);
		chckbxDaos = new JCheckBox("DAOs");
		chckbxDaos.setSelected(true);
		chckbxDaos.setBounds(206, 230, 65, 23);
		frmUtilitrioDeEngenharia.getContentPane().add(chckbxDaos);
		chckbxLibraries = new JCheckBox("Libraries");
		chckbxLibraries.setSelected(true);
		chckbxLibraries.setBounds(273, 230, 97, 23);
		frmUtilitrioDeEngenharia.getContentPane().add(chckbxLibraries);
		loadOptions();
	}

	private void verify(){
		if (btnConcluir == null)
			return;
		btnConcluir.setEnabled(false);
		if (txtUrl.getText().isEmpty())
			return;
		if (txtBanco.getText().isEmpty())
			return;
		if (txtUsuario.getText().isEmpty())
			return;
		if (dir == null)
			return;
		btnConcluir.setEnabled(true);
	}

	private void buscaPasta(){
		JFileChooser chooser = new JFileChooser();
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (dir != null)
			chooser.setCurrentDirectory(dir);
		chooser.showOpenDialog(null);
		dir = chooser.getSelectedFile();
		if (dir != null)
			txtPasta.setText(dir.getAbsolutePath());
		verify();
	}

	private Server getFactory(){
		char[] caPassword = txtSenha.getPassword();
		StringBuilder builder = new StringBuilder();
		for (char c : caPassword) {
			builder.append(c);
		}
		String url = txtUrl.getText()+txtBanco.getText();
		String username = txtUsuario.getText();
		String password = builder.toString();
		ConnectionFactory connectionFactory = new ConnectionFactory(url, username, password);
		try {
			connectionFactory.testConnection();
			MysqlServer server = new MysqlServer();
			server.setProperties(url, username, password);
			return server;
		} catch (SQLException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, "Não foi possivel conectar com o banco com os dados informados!", "Erro", JOptionPane.WARNING_MESSAGE);
		}
		return null;
	}

	@SuppressWarnings("resource")
	public static void copyFile(File source, File destination) throws IOException {
	    if (destination.exists())
	        destination.delete();
	    FileChannel sourceChannel = null;
	    FileChannel destinationChannel = null;
	    try {
	        sourceChannel = new FileInputStream(source).getChannel();
	        destinationChannel = new FileOutputStream(destination).getChannel();
	        sourceChannel.transferTo(0, sourceChannel.size(),
	                destinationChannel);
	    } finally {
	        if (sourceChannel != null && sourceChannel.isOpen())
	            sourceChannel.close();
	        if (destinationChannel != null && destinationChannel.isOpen())
	            destinationChannel.close();
	   }
	}

	public void copyLibraries(Server server){
		try {
			File mDir = new File(dir.getAbsolutePath()+"/model/data/");
			mDir.mkdirs();
			File bDir = new File("data");
			File[] files = bDir.listFiles();
			for (File file : files) {
				BufferedReader br = new BufferedReader(new FileReader(file));
				File fileToWrite = new File(mDir+"/"+file.getName());
				BufferedWriter bw = new BufferedWriter(new FileWriter(fileToWrite));
				while (br.ready()){
					String line = br.readLine();
					//Le gambiarra
					if (server.getKeyGen() != null
					&& file.getName().compareTo("DAO.java") == 0
					&& line.compareTo("	protected String lastIdSql = null;") == 0)
						line = "	protected String lastIdSql = \""+server.getKeyGen()+"\";";
					bw.write(line);
					bw.newLine();
				}
				br.close();
				bw.close();
			}
			File origin = new File("engrev.project_lib/mysql-connector-java-5.1.7-bin.jar");
			File libFolder = new File(dir.getParentFile().getAbsolutePath()+"/lib/");
			libFolder.mkdirs();
			File destiny = new File(dir.getParentFile().getAbsolutePath()+"/lib/mysql-connector-java-5.1.7-bin.jar");
			copyFile(origin, destiny);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void loadOptions(){
		File fileProp = new File("er.properties");
		if (!fileProp.exists())
			return;
		Properties prop = new Properties();
		FileInputStream in;
		try {
			in = new FileInputStream(fileProp);
			prop.load(in);
			String url = prop.getProperty("jdbc.url");
			if (url != null)
				txtUrl.setText(url);
			String banco = prop.getProperty("jdbc.database");
			if (banco != null)
				txtBanco.setText(banco);
			String username = prop.getProperty("jdbc.username");
			if (username != null)
				txtUsuario.setText(username);
			String password = prop.getProperty("jdbc.password");
			if (password != null)
				txtSenha.setText(password);
			String dirPath = prop.getProperty("export.dir");
			dir = new File(dirPath);
			txtPasta.setText(dir.getAbsolutePath());
			in.close();
			verify();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void saveOptions(){
		try {
			File fileProp = new File("er.properties");
			if (!fileProp.exists())
				fileProp.createNewFile();
			StringBuilder builder = new StringBuilder();
			builder.append("jdbc.url=").append(txtUrl.getText()).append("\n");
			builder.append("jdbc.database=").append(txtBanco.getText()).append("\n");
			builder.append("jdbc.username=").append(txtUsuario.getText()).append("\n");
			char[] caPassword = txtSenha.getPassword();
			StringBuilder pBuilder = new StringBuilder();
			for (char c : caPassword) {
				pBuilder.append(c);
			}
			builder.append("jdbc.password=").append(pBuilder.toString()).append("\n");
			builder.append("export.dir=").append(dir.getAbsolutePath().replace('\\', '/'));
			FileWriter write = new FileWriter(fileProp);
			write.write(builder.toString());
			write.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void export() throws IOException{
		Server server = getFactory();
		if (server == null)
			return;
		DatabaseFactory factory = new DatabaseFactory(txtBanco.getText(), server);
		Database database = factory.getDatabase();
		List<FileData> lm;
		if (chckbxModel.isSelected()){
			lm = new ModelExporter(database).export();
			for (FileData fileData : lm) {
				String fileName = dir.getAbsolutePath()+"/model/"+fileData.getFileName();
				File pDir = new File(dir.getAbsolutePath()+"/model/");
				pDir.mkdirs();
				File file = new File(fileName);
				file.createNewFile();
				FileWriter write = new FileWriter(file);
				write.write(fileData.getFileContent());
				write.close();
			}
		}
		if (chckbxDaos.isSelected()){
			lm = new DAOExporter(database).export();
			for (FileData fileData : lm) {
				String fileName = dir.getAbsolutePath()+"/model/dao/"+fileData.getFileName();
				File pDir = new File(dir.getAbsolutePath()+"/model/dao/");
				pDir.mkdirs();
				File file = new File(fileName);
				file.createNewFile();
				FileWriter write = new FileWriter(file);
				write.write(fileData.getFileContent());
				write.close();
			}
		}
		if (chckbxLibraries.isSelected()){
			copyLibraries(server);
		}
		//Save properties
		FileData fileData = server.getProperties();
		File pDir = dir.getParentFile();
		File file = new File(pDir.getAbsolutePath()+"/"+fileData.getFileName());
		FileWriter write = new FileWriter(file);
		write.write(fileData.getFileContent());
		write.close();
		saveOptions();
		JOptionPane.showMessageDialog(frmUtilitrioDeEngenharia, "Concluído", "Confirmação", JOptionPane.INFORMATION_MESSAGE);
	}
}
