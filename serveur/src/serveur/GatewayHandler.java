package serveur;

import java.io.IOException;

import common.Link;
import common.Trame;
import common.Trame_message;

public class GatewayHandler {

	private Thread clientThread;
	private Server serverAttached;
	private Link link;

	public GatewayHandler(Link link, Server server) throws IOException {
		this.link = link;
		this.serverAttached = server;
		this.clientThread = new Thread(() -> {
			// attend de nouveau message
			while (true) {
				try {
					Trame trame = link.receive();
					if (trame != null) {
						// Si c'est un message
						if (trame.getType_message() == 2) {
							serverMessageReceived((Trame_message) trame);
						} else {
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
	public void serverMessageReceived(Trame_message received) throws IOException {
		System.out.println("Received Trame_message from " + link.getSocket().getInetAddress().toString());
		// Si le client est sur le serveur local
		if (received.getServeur_cible().compareTo(this.serverAttached.id) == 0) {
			serverAttached.sendTrameMessageLocally(received);
		}
		else {
			serverAttached.sendTrameMessageExternaly(received);
		}
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
