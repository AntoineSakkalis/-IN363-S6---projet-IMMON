package serveur;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.IOException;
import java.net.Inet4Address;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;

import common.Link;
import common.Trame;
import common.Trame_message;
import common.Trame_routage;

public class Server {

	private String id = "S01";
	private HashMap<String, Link> routageClientTable = new HashMap<>(); // NomDuClient,AdresseIPàQuiEnvoyer
	
	private ArrayList<String> serveurs;
	private ArrayList<Inet4Address> passerelles;
	private ArrayList<ArrayList<String>> clients_serveurs;	
	private ArrayList<Integer> distance;
	
	private int port = 9081;

	public Server() throws ClassNotFoundException {

		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Server listening on port " + port);

			while (true) {

				// new client
				Socket clientSocket = serverSocket.accept();
				Link link = new Link(clientSocket);
				System.out.println("Device connected: " + link.getSocket().getInetAddress());

				// lecture 1er message
				Trame received = link.receive();
				System.out.println("Received: " + received);

				if (received.getType_message() == 2) {
					Trame_message trameMsg = (Trame_message) received;
					//ajout nouveau client
					if (routageClientTable.get(trameMsg.getClient_source()) == null) {
						routageClientTable.put(trameMsg.getClient_source(), link);
						ClientHandler c = new ClientHandler(link);
						c.clientMessageReceived((Trame_message) trameMsg);
					}
					
				}
				else if (received.getType_message() == 1) {
					Trame_routage TrameRtg = (Trame_routage) received;
					if(compareTableRoutage(TrameRtg)){
						
					}
				}
			}

		}
		catch(IOException e){
			System.err.println("Could not start server: " + e.getMessage());
			e.printStackTrace();
		}
	}

	//Compare la table de routage partagée à la locale, si elles sont identiques, return true, sinon false
	private boolean compareTableRoutage(Trame_routage trame){
		return true;
	}
	
	public static void main(String[] args) throws ClassNotFoundException {
		new Server();
	}
}
