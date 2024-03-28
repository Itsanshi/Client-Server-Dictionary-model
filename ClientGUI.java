import javax.swing.*;
import javax.swing.border.*;
import java.awt.*;
import java.io.*;
import java.net.Socket;
import java.util.List;
import java.util.ArrayList;
public class ClientGUI extends JFrame {
	private JTextField keywordField;
	private JTextField optionField;
	private JButton generateDictionaryButton;
	private JButton saveDictionaryButton;
	private JTextArea dictionaryTextArea;
	static int portNumber = 8081;
	private Socket socket;
	private PrintWriter out;
	private BufferedReader in;
	public ClientGUI()
	{
		setTitle("Crunch Security Client");
		setSize(900, 400);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		initComponents();
		addComponents();
		addListeners();
		centerFrame();
		addDesign();
		initializeSocket();
	}
	private void initializeSocket()
	{
		try
		{
			socket = new Socket("localhost", portNumber);
			out = new PrintWriter(socket.getOutputStream(), true);
			in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
		}
		catch (IOException e)
		{
			e.printStackTrace();
		}
	}
	private void initComponents()
	{
		keywordField = createTextField(20);
		optionField = createTextField(20);
		generateDictionaryButton = new JButton("Generate Dictionary");
		saveDictionaryButton = new JButton("Save Dictionary");
		saveDictionaryButton.setEnabled(false);
		dictionaryTextArea = new JTextArea();
		dictionaryTextArea.setEditable(false);
	}
	private JTextField createTextField(int columns)
	{
		JTextField textField = new JTextField(columns);
		textField.setFont(new Font("Dialog", Font.PLAIN, 14));
		return textField;
	}
	private void addComponents()
	{
		setLayout(new BorderLayout());
		JPanel inputPanel = new JPanel();
		inputPanel.add(createInputPanel("Keywords: ", keywordField));
		inputPanel.add(createInputPanel("Options: ", optionField));
		inputPanel.add(generateDictionaryButton);
		inputPanel.add(saveDictionaryButton);
		add(inputPanel, BorderLayout.CENTER);
		JScrollPane scrollPane = new JScrollPane(dictionaryTextArea);
		scrollPane.setPreferredSize(new Dimension(400, 200));
		add(scrollPane, BorderLayout.EAST);
	}
	private JPanel createInputPanel(String label, JTextField textField)
	{
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 10, 10));
		panel.add(new JLabel(label));
		panel.add(textField);
		return panel;
	}
	private void addListeners()
	{
		generateDictionaryButton.addActionListener(e -> handleGenerateDictionary());
		saveDictionaryButton.addActionListener(e -> handleSaveDictionary());
	}
	private void handleGenerateDictionary()
	{
		String keyword = keywordField.getText();
		String option = optionField.getText();
		if ( !keyword.isEmpty() && !option.isEmpty())
		{
			out.println("generateDictionary:" + keyword + ":" + option);
			try
			{
				StringBuilder dictionaryBuilder = new StringBuilder();
				String entry;
				while (!(entry = in.readLine()).equals("EndDictionary"))
				{
					dictionaryBuilder.append(entry).append("\n");
				}
				dictionaryTextArea.setText(dictionaryBuilder.toString().trim());
				saveDictionaryButton.setEnabled(true);
				JOptionPane.showMessageDialog(this, "Dictionary generated!");
			}
			catch (IOException e)
			{
				JOptionPane.showMessageDialog(this, "Dictionary not generated !!! Internal Error");
				e.printStackTrace();
			}
		}
		else
		{
			JOptionPane.showMessageDialog(this, "Please provide valid inputs for keyword and option.");
		}
	}

	private void handleSaveDictionary()
	{
		try
		{
			String displayedDictionary = dictionaryTextArea.getText();
			String keywordsHeader = "Keywords: " + keywordField.getText();
			String optionsHeader = "Options: " + optionField.getText();
			String contentToSave = keywordsHeader + "\t" + optionsHeader + "\nDictionary contents:\n" + displayedDictionary;
			JFileChooser fileChooser = new JFileChooser();
			int returnValue = fileChooser.showSaveDialog(this);
			if (returnValue == JFileChooser.APPROVE_OPTION)
			{
				String filePath = fileChooser.getSelectedFile().getPath();
				if (saveDictionaryToFile(contentToSave, filePath))
				{
					JOptionPane.showMessageDialog(this, "Dictionary saved to: " + filePath);
				}
				else
				{
					JOptionPane.showMessageDialog(this, "Failed to save dictionary.");
				}
			}
		}
		catch (Exception e)
		{
			e.printStackTrace();
		}
	}
	private boolean saveDictionaryToFile(String dictionary, String filePath)
	{
		try (PrintWriter writer = new PrintWriter(filePath))
		{
			String[] entries = dictionary.split("\n");
			for (String entry : entries)
			{
				writer.println(entry);
			}
			return true;
		}
		catch (IOException e)
		{
			e.printStackTrace();
			return false;
		}
	}

	private void centerFrame()
	{
		setLocationRelativeTo(null);
	}

	private void addDesign()
	{
		int borderThickness = 2;
		LineBorder lineBorder = new LineBorder(Color.LIGHT_GRAY, borderThickness);
		getRootPane().setBorder(BorderFactory.createCompoundBorder(lineBorder,BorderFactory.createEmptyBorder(borderThickness, borderThickness, borderThickness,borderThickness)));
	}

	public static void main(String[] args)
	{
		SwingUtilities.invokeLater(() -> new ClientGUI().setVisible(true));
	}
}
