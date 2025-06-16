package serveur;

import java.io.IOException;
import java.net.Inet4Address;

import common.Link;
import common.Trame;
import common.Trame_message;
import common.Trame_routage;

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
						//Si c'est pour moi
						if(trame.getServeur_cible().compareTo(this.serverAttached.id) == 0) {
							if (trame.getType_message() == 2) {
								serverMessageReceived((Trame_message) trame);
							}
							else if (trame.getType_message() == 1) {
								serverRoutageReceived((Trame_routage) trame);
							} else {
								throw new RuntimeException("Type de trame reçue non valide");
							}
						}
						//Sinon je redirige
						else {
							serverAttached.sendTrameExternaly(trame);
							System.out.println(serverAttached.id + ": Trame reçue " + trame.getServeur_source() + "->" + trame.getServeur_cible());
						}
					}
				} catch (IOException e) {
					throw new RuntimeException("Format de trame reçu non valide");
				}
			}
		});
		this.clientThread.start();
	}

	// traite les messages entrants local
	public void serverMessageReceived(Trame_message received) throws IOException {
		System.out.println(serverAttached.id +": Received Trame_message from " + link.getSocket().getInetAddress().toString());
		serverAttached.sendTrameMessageLocally(received);
	}
	
	//traite les trames routages entrantes
	public void serverRoutageReceived(Trame_routage received) {
		
		System.out.println(serverAttached.id +": Table de routage reçue de " + received.getServeur_source() + " : " + received.getServeurs() + received.getClients_serveurs());
		//update si nécessaire
		if(!serverAttached.compareTableRoutage(received)){
			//maj
			serverAttached.updateTableRoutage(received, (Inet4Address) link.getSocket().getInetAddress());
			System.out.println(serverAttached.id +": Table de routage mis à jour");
			serverAttached.printTableRoutage();
			//broadCast à tous les voisins
			serverAttached.broadCastTableRoutage();
			//si la table locale à plus d'entrée que celle reçue, envoi de la table la plus à jour (la nôtre)
//			if(!serverAttached.compareTableRoutage(received)){
//				serverAttached.sendTrameRoutage(received.getServeur_source());
//			}
		}
		else {
			System.out.println(serverAttached.id +": Table de routage déjà à jour");
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
