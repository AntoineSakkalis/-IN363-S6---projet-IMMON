package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.net.Inet4Address;
import java.net.InetAddress;

import common.Link;
import common.Trame;
import common.Trame_connect;
import common.Trame_message;

public class Client {

	private String name = "C1";
	private int port = 9081;
	private String serverToConnect = "S01";
	private Inet4Address ipGateway;
	private int mode = 0; //temporaire tant que classe Client pas fini, 0 : écoute, 1 : envoi d'un paquet de C1-S02 à C1-S01

	public Client(String name, int port, String serverToConnect, Inet4Address ipGateway, int mode) {
		
		this.name = name;
		this.port = port;
		this.serverToConnect = serverToConnect;
		this.ipGateway = ipGateway;
		this.mode = mode;
		
		runClient();

	}
	
	public Client() {
		
		try {
			ipGateway = (Inet4Address) InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		
		runClient();
	}

	public void runClient() {
		try {
			
			Link link = new Link(ipGateway, port);
			
			Trame_connect connect = new Trame_connect(this.serverToConnect, this.serverToConnect, this.name, false);
			link.send(connect);
			connect = (Trame_connect) link.receive();
			
			if(connect.isApproval()) { //si connexion réussie
				System.out.println("Serveur connecté !");
				
				if(this.mode == 0) {
					Trame_message response = null;
					response = (Trame_message) link.receive();
					System.out.println("Received: " + response.getDu());
				}
				else if(this.mode == 1) {
					Trame_message response = new Trame_message("S01", "S02", "C1", "C1", "c'est bon!");;
					link.send(response);
				}
				
				///////////////////////////////////////////BOUCLE CLIENT
//				Trame_message trame = new Trame_message(this.ServerToConnect, this.ServerToConnect, this.name, this.name, "c'est bon!");
//				link.send(trame);
//				Trame_message response = null;
//				while (response == null) {
//					response = (Trame_message) link.receive();
//				}
//				System.out.println("Received: " + response.getDu());
//				
//				Trame_message trame2 = new Trame_message("S01", "S01", "C1", "C1", "c'est bon!2");
//				link.send(trame2);
//				Trame_message response2 = null;
//				while (response2 == null) {
//					response2 = (Trame_message) link.receive();
//				}
//				System.out.println("Received: " + response2.getDu());
			}
			else {
				throw new RuntimeException("Connexion au serveur impossible");
			}
			
			
			link.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static void main(String[] args) {
		new Client();
	}

}
