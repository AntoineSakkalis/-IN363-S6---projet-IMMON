package serveur;

import java.io.IOException;

import common.Link;
import common.Trame;
import common.Trame_message;

public class ClientHandler {

	private Thread clientThread;
	private Link link;

	public ClientHandler(Link link) throws IOException {
		this.link = link;
		this.clientThread = new Thread(() -> {
			// attend de nouveau message
			while (true) {
				try {
					Trame message = link.receive();
					if (message != null) {
						// Si c'est bien un message
						if (message.getType_message() == 2) {
							clientMessageReceived((Trame_message) message);
						} else {
							throw new RuntimeException("Type de trame reçu non valide");
						}
					}
				} catch (IOException e) {
					throw new RuntimeException("Format de trame reçu non valide");
				}
			}
		});
	}

	// traite les messages entrants
	public void clientMessageReceived(Trame_message received) throws IOException {
		// Si le client est sur le serveur local
		if (received.getServeur_cible().compareTo("S01") == 0) {
			this.link.send(received);
		}
		////////////////////////////////////////////////////
	}
}
