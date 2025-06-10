package client;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import fr.insa.reseau.common.Link;
import fr.insa.reseau.common.Trame;

public class Client {
	
	private int id = 0;
	private int port = 9082;
	private String ipGateway = "127.0.0.1";
	
	public Client() {
        try {
            Link link = new Link(ipGateway, port);
            Trame trame = Trame.fromLine("0;0;1;"+ipGateway+";"+ipGateway+";c'est bon!;");
            link.send(trame.toLine());
            String response = link.receive();
            System.out.println("Received: " + response);
            link.close();
        } catch (IOException | ClassNotFoundException e) {
            e.printStackTrace();
        }
        
		
	}


}


