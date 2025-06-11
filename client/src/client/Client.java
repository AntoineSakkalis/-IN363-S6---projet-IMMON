package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import common.Link;
import common.Trame;
import common.Trame_message;

public class Client {

	private int id = 0;
	private int port = 9081;
	private String ipGateway = "127.0.0.1";

	public Client() {
		try {
			Link link = new Link(ipGateway, port);
			Trame_message trame = new Trame_message(2, "S01", "S01", "C1", "C1", "c'est bon!");
			link.send(trame);
			Trame response = null;
			while (response == null) {
				response = link.receive();
			}
			System.out.println("Received: " + response);
			link.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	public static void main(String[] args) {
		new Client();
	}

}
