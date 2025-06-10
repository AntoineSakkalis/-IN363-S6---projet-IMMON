package serveur;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class Server {

	private int port = 9082;

	public Server() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Server listening on port " + port);

			while (true) {
				try (Socket clientSocket = serverSocket.accept();
				     ObjectOutputStream out = new ObjectOutputStream(clientSocket.getOutputStream());
				     ObjectInputStream in = new ObjectInputStream(clientSocket.getInputStream())) {

					System.out.println("Client connected: " + clientSocket.getInetAddress());

					Object obj = in.readObject();
					if (obj instanceof String) {
						String received = (String) obj;
						System.out.println("Received: " + received);

						String response = "Hello from server!";
						out.writeObject(response);
						out.flush();
					} else {
						System.out.println("Received unknown object type.");
					}
				} catch (IOException | ClassNotFoundException e) {
					System.err.println("Error handling client: " + e.getMessage());
					e.printStackTrace();
				}
			}
		} catch (IOException e) {
			System.err.println("Could not start server: " + e.getMessage());
			e.printStackTrace();
		}
	}

	public static void main(String[] args) {
		new Server();
	}
}
