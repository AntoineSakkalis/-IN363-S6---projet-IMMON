package serveur;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;

import common.Link;
import common.Trame;
import common.Trame_connect;
import common.Trame_getClients;
import common.Trame_message;
import common.Trame_routage;

public class Server {

	public String id = "S01";
	private HashMap<String, ClientHandler> routageClientTableLocal = new HashMap<>(); // NomDuClient,ObjetEnChargeCanal
	private HashMap<Inet4Address, GatewayHandler> routageNeighborTable = new HashMap<>(); //AdresseIPPasserelle,ObjetEnChargeCanal
	
	private ArrayList<String> serveurs = new ArrayList<String>();
	private ArrayList<Inet4Address> passerelles = new ArrayList<Inet4Address>();
	private ArrayList<ArrayList<String>> clients_serveurs = new ArrayList<ArrayList<String>>();	
	private ArrayList<Integer> distance = new ArrayList<Integer>();
	private ArrayList<Integer> ports = new ArrayList<Integer>(); //dispensable, utile pour lancer 2 server sur son pc
	
	private int port = 9081;

	public Server(String id, int port, ArrayList<String> serveurs, ArrayList<Inet4Address> passerelles, ArrayList<ArrayList<String>> clients_serveurs, ArrayList<Integer> distance, ArrayList<Integer> ports) {
		this.id = id;
		this.port = port;
		this.serveurs = serveurs;
		this.passerelles = passerelles;
		this.clients_serveurs = clients_serveurs;
		this.distance = distance;
		this.ports = ports;
		runServer();
	}
		
	public Server() {
		
		Inet4Address gateway;
		try {
		    gateway = (Inet4Address) InetAddress.getByName("192.168.127.183");
		} catch (UnknownHostException e) {
		    e.printStackTrace();
		    return;
		}
		
		serveurs.add(id);
		passerelles.add(gateway);
		clients_serveurs.add(new ArrayList<String>());
		distance.add(0);
		
		runServer();
	}
	
	private void runServer() {
		try (ServerSocket serverSocket = new ServerSocket(port)) {
			System.out.println("Server listening on port " + port);

			while (true) {

				Socket socket = serverSocket.accept();
				Link link = new Link(socket);

				// lecture 1er message
				Trame received = link.receive();

				// client
				if(received.getServeur_source().compareTo(this.id) == 0) {
					
					if(received.getType_message() == 3) { //Si c'est bien un message de début de connexion
						Trame_connect trameConnect = (Trame_connect) received;
						
						if (routageClientTableLocal.get(trameConnect.getClient()) == null) { //si le client n'est pas déjà connecté
							//ajout info routage
							clients_serveurs.get(serveurs.indexOf(id)).add(trameConnect.getClient());
							//création canal
							ClientHandler c = new ClientHandler(link, this);
							routageClientTableLocal.put(trameConnect.getClient(), c);
							//approbation
							trameConnect.setApproval(true);
							//envoi de la réponse
							c.sendTrame(trameConnect);
							System.out.println("Client connecté: " + trameConnect.getClient());
						}
						else {
							routageClientTableLocal.get(trameConnect.getClient()).sendTrame(trameConnect); //renvoie un nope
							System.out.println("Erreur de connexion client : Vous ne pouvez pas connecter 2 fois le même client au serveur.");
						}
					}
					else {
						System.out.println("Erreur de connexion cliente : la trame n'est pas de type Trame_connect, Il faut d'abord établir une connexion (Trame de type 3) avant d'envoyer d'autres paquets");
					}
				}
				
				// serveur
				else {
					System.out.println("Serveur connecté : " + link.getSocket().getInetAddress());
					//ajout si nouveau chemin
					if(routageNeighborTable.get(link.getSocket().getInetAddress()) == null) {
						
						GatewayHandler g = new GatewayHandler(link, this);
						routageNeighborTable.put((Inet4Address) link.getSocket().getInetAddress(), g);
						
					}
					
					//ajout si nouveau serveur
					if (searchServer(received.getServeur_source()) == -1) {
						
						this.serveurs.add(received.getServeur_source()); 						//nom du serveur source
						this.clients_serveurs.add(null); 										//pas d'information pour l'instant
						this.passerelles.add((Inet4Address) link.getSocket().getInetAddress()); //par où il est passé pour arriver
						this.distance.add(null);												//pas d'information pour l'instant
						
					}
					
					//TRAME MESSAGE
					if (received.getType_message() == 2) {
						Trame_message trameMsg = (Trame_message) received;
						routageNeighborTable.get(link.getSocket().getInetAddress()).serverMessageReceived(trameMsg);
					}
					//TRAME ROUTAGE
					else if (received.getType_message() == 1) {
						Trame_routage trameRtg = (Trame_routage) received;
						routageNeighborTable.get(link.getSocket().getInetAddress()).serverRoutageReceived(trameRtg);
					}
				}
				
			}

		}
		catch(IOException e){
			System.err.println("Could not start server: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
	//MESSAGE
	//envoie un message à un client local
	public void sendTrameMessageLocally(Trame_message t) {
		if(routageClientTableLocal.get(t.getClient_cible()) != null) {
			routageClientTableLocal.get(t.getClient_cible()).sendTrame(t);
			System.out.println("Transmit to " + t.getClient_cible());
		}
		else {
			System.out.println("Transmission impossible : le client local n'existe pas.");
		}
	}
	
	//envoie un paquet vers un autre serveur
	public void sendTrameExternaly(Trame t) {
		if(searchServer(t.getServeur_cible()) != -1) { //Si le serveur est connu
			if(routageNeighborTable.get(this.passerelles.get(searchServer(t.getServeur_cible()))) != null) { //si le canal de communication existe
				routageNeighborTable.get(this.passerelles.get(searchServer(t.getServeur_cible()))).sendTrame(t);
				System.out.println("Transmit to " + routageNeighborTable.get(this.passerelles.get(searchServer(t.getServeur_cible()))).getLink().getSocket().getInetAddress().toString());
			}
			else {
				try { //on essaie de le créer et de l'envoyer avec les informations qu'on a
					GatewayHandler g = new GatewayHandler(new Link(passerelles.get(searchServer(t.getServeur_cible())), ports.get(searchServer(t.getServeur_cible()))), this);
					routageNeighborTable.put((Inet4Address) passerelles.get(searchServer(t.getServeur_cible())), g);
					routageNeighborTable.get(this.passerelles.get(searchServer(t.getServeur_cible()))).sendTrame(t);
					System.out.println("Transmit to " + routageNeighborTable.get(this.passerelles.get(searchServer(t.getServeur_cible()))).getLink().getSocket().getInetAddress().toString());
				} catch (IOException e) {
					System.out.println(e);
					System.out.println("Transmission impossible : Le canal de communication n'existe pas et n'est pas constructible");
				}
			}
		}
		else {
			System.out.println("Transmission impossible : Serveur cible inconnu");
		}
	}
	
	//ROUTAGE
	//Compare la table de routage partagée à la locale, si elles ont les mêmes clients associés aux mêmes serveurs, return true, sinon false
	public boolean compareTableRoutage(Trame_routage trame){
		
		//comparaison taille des tableaux
		if((trame.getServeurs().size() != this.serveurs.size())
			|| (trame.getClients_serveurs().size() != this.clients_serveurs.size())
			|| (trame.getPasserelles().size() != this.passerelles.size())
			|| (trame.getDistance().size() != this.distance.size())) {
			return false;
		}
		
		//comparaison contenu tableau
		for(String nom : trame.getServeurs()) {
			
			if(!this.serveurs.contains(nom)) {
				return false;
			}
			
			int indexIn = this.serveurs.indexOf(nom); 		//index du côté local
			int indexOut = trame.getServeurs().indexOf(nom);//index du côté du serveur qui nous envoie
			
			//vérification mêmes clients pour chaque serveur
			for(int client = 0; client < trame.getClients_serveurs().get(indexOut).size(); client++) {
				if(trame.getClients_serveurs().get(indexOut).get(client).compareTo(this.clients_serveurs.get(indexIn).get(client)) != 0) {
					return false;
				}
			}
			
		}
		return true;
	}
	
	//met à jour la table de routage
	public void updateTableRoutage(Trame_routage trame, Inet4Address gateway) {
		
		for(String nom : trame.getServeurs()) {
			
			int indexOut = trame.getServeurs().indexOf(nom);//index du côté du serveur qui nous envoie
			
			//Si ce serveur n'est pas connu en local 
			if(!this.serveurs.contains(nom)) {
				
				this.serveurs.add(nom);
				this.clients_serveurs.add(trame.getClients_serveurs().get(indexOut));
				this.distance.add(trame.getDistance().get(indexOut) + 1);
				this.passerelles.add(gateway);
				
			}
			//si ce server est connu
			else {
				
				int indexIn = this.serveurs.indexOf(nom); //index du côté local
				
				this.clients_serveurs.set(indexIn, trame.getClients_serveurs().get(indexOut));
				this.distance.set(indexIn, trame.getDistance().get(indexOut) + 1);
				this.passerelles.set(indexIn, gateway);
				
			}
			
		}
		
	}
	
	//envoie une trame routage au serveur donné
	public void sendTrameRoutage(String server_Source) {
		sendTrameExternaly(new Trame_routage(server_Source,this.id,this.serveurs, this.passerelles, this.clients_serveurs, this.distance));
	}
	
	//GETLISTCLIENT
	public void sendClientList(Trame_getClients trame) {
		//si le client existe
		if(routageClientTableLocal.get(trame.getClient_source()) != null) {
			//construction trame
			for(int i = 0; i < this.clients_serveurs.size(); i++) {
				for(int j = 0; j < this.clients_serveurs.get(i).size(); j++) {
					trame.getListCli().add(this.clients_serveurs.get(i).get(j));
				}
			}
			
			//envoi
			routageClientTableLocal.get(trame.getClient_source()).sendTrame(trame);
			System.out.println("Transmit to " + trame.getClient_source());
		}
		else {
			System.out.println("Transmission impossible : le client local n'existe pas.");
		}
	}
	
	//renvoie l'index du nom d'un serveur du tableau serveurs, -1 si aucune occurence
	private int searchServer(String server) {
		return this.serveurs.indexOf(server);
	}
	
	//renvoie le nom du server auquel le client est rattaché, null sinon
	public String searchServerOfClient(String client) {
		for(int index_server = 0; index_server < this.serveurs.size(); index_server++) {
			for(int index_client = 0; index_client < this.clients_serveurs.get(index_server).size(); index_client++) {
				if(this.clients_serveurs.get(index_server).get(index_client).compareTo(client) == 0) {
					return this.serveurs.get(index_server);
				}
			}
		}
		return null;
	}
	
	public static void main(String[] args) {
		new Server();
	}
}
