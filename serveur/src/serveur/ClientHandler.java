package serveur;

import java.io.IOException;

import common.Link;
import common.Trame;
import common.Trame_getClients;
import common.Trame_message;

public class ClientHandler {

	private Thread clientThread;
	private Server serverAttached;
	private Link link;

	public ClientHandler(Link link, Server server) throws IOException {
		this.link = link;
		this.serverAttached = server;
		this.clientThread = new Thread(() -> {
			// attend de nouveau message
			while (true) {
				try {
					Trame trame = link.receive();
					if (trame != null) {
						// Si c'est bien un message
						if (trame.getType_message() == 2) {
							clientMessageReceived((Trame_message) trame);
						}
						//trame getClients
						else if (trame.getType_message() == 4) {
							clientGetClientsReceived((Trame_getClients) trame);
						}
						else {
							throw new RuntimeException("Type de trame reçu non valide");
						}
					}
				} catch (IOException e) {
					throw new RuntimeException("Format de trame reçu non valide");
				}
			}
		});
		this.clientThread.start();
	}

	// traite les messages entrants
	public void clientMessageReceived(Trame_message received) throws IOException {
		System.out.println("Received Trame_message from " + received.getClient_source());
		//ajout du serveur cible si trouvé, sinon le message n'est pas transmis
		if(serverAttached.searchServerOfClient(received.getClient_cible()) != null) {
		
			received.setServeur_cible(serverAttached.searchServerOfClient(received.getClient_cible()));
		
			// Si le client est sur le serveur local
			if (received.getServeur_cible().compareTo(this.serverAttached.id) == 0) {
				serverAttached.sendTrameMessageLocally(received);
			}
			else {
				serverAttached.sendTrameExternaly(received);
			}
		
		}
		else {
			System.out.println("Erreur : Le client cible choisi n'est pas connu");
		}
	}
	
	//traite les demandes de récupération de la liste de clients du réseau
	public void clientGetClientsReceived(Trame_getClients trame) {
		serverAttached.sendClientList(trame);
	}
	
	public Link getLink() {
		return link;
	}

	public void setLink(Link link) {
		this.link = link;
	}
	
	// envoie une trame
	public void sendTrame(Trame t) {
		try {
			this.link.send(t);
		} catch (IOException e) {
			System.out.println(e);
		}
	}
}
