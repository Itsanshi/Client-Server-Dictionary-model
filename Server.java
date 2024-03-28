import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.Collections;
public class Server {
	public static void main(String[] args) {
		int portNumber = 8081;
		try {
			ServerSocket serverSocket = new ServerSocket(portNumber);
			System.out.println("Server is listening on port " + portNumber);
			while (true) {
				Socket clientSocket = serverSocket.accept();
				System.out.println("Client connected: " + clientSocket.getInetAddress().getHostAddress());
				new ClientHandler(clientSocket).start();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private static class ClientHandler extends Thread {
		private final Socket clientSocket;
		public ClientHandler(Socket socket) {
			this.clientSocket = socket;
		}
		@Override
		public void run() {
			try (
					PrintWriter out = new PrintWriter(clientSocket.getOutputStream(), true);
					BufferedReader in = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()))
					) {
				String request;
				while ((request = in.readLine()) != null) {
					System.out.println("Received request from client: " + request);
					processRequest(request, out);
				}
			} catch (IOException e) {
				e.printStackTrace();
			} finally {
				try {
					clientSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		private void processRequest(String request, PrintWriter out) {
			String[] parts = request.split(":");
			String command = parts[0];
			switch (command) {
			case "generateDictionary":
				handleGenerateDictionary(parts[1], parts[2], out);
				break;
			case "saveDictionary":
				handleSaveDictionary(out);
				break;
			default:
				break;
			}
		}
		private void handleGenerateDictionary(String keyword, String option, PrintWriter out) {
			List<String> dictionary = generateDictionary(keyword, option);
			sendDictionary(out, dictionary);
		}
		private List<String> generateDictionary(String keyword, String option) {
			List<String> dictionary = new ArrayList<>();
			List<Character> characters = new ArrayList<>();
			String word = keyword + option;
			for (char c : word.toCharArray()) {
				characters.add(c);
			}
			for (int i = 0; i <= keyword.length() + option.length(); i++) {
				Collections.shuffle(characters);
				StringBuilder shuffledWord = new StringBuilder();
				for (char c : characters) {
					shuffledWord.append(c);
				}
				dictionary.add(shuffledWord.toString());
			}
			return dictionary;
		}
		private void sendDictionary(PrintWriter out, List<String> dictionary) {
			for (String word : dictionary) {
				out.println(word);
			}
			out.println("EndDictionary");
		}
		private void handleSaveDictionary(PrintWriter out) {
			out.println("Dictionary saved");
		}
	}
}
