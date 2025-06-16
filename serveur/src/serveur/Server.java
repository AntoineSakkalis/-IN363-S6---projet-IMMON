package serveur;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;

import common.Link;
import common.Trame;
import common.Trame_connect;
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
							ClientHandler c = new ClientHandler(link, this);
							routageClientTableLocal.put(trameConnect.getClient(), c);
							trameConnect.setApproval(true);
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
					
					if (received.getType_message() == 2) {
						Trame_message trameMsg = (Trame_message) received;
						routageNeighborTable.get(link.getSocket().getInetAddress()).serverMessageReceived(trameMsg);
					}
					else if (received.getType_message() == 1) {
						Trame_routage TrameRtg = (Trame_routage) received;
						if(compareTableRoutage(TrameRtg)){
							/////////////////////////////////////////////////////
						}
					}
				}
				
			}

		}
		catch(IOException e){
			System.err.println("Could not start server: " + e.getMessage());
			e.printStackTrace();
		}
	}
	
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
	
	//redirige un paquet vers un autre serveur
	public void sendTrameMessageExternaly(Trame_message t) {
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

	//Compare la table de routage partagée à la locale, si elles sont identiques, return true, sinon false
	private boolean compareTableRoutage(Trame_routage trame){
		///////////////////////////////////////////////////////////////////////
		return true;
	}
	
	//renvoie l'index du nom d'un serveur du tableau serveurs, -1 si aucune occurence
	private int searchServer(String server) {
		return this.serveurs.indexOf(server);
	}
	
	//renvoie l'index de l'adresse IP d'un serveur du tableau passerelles, -1 si aucune occurence
	private int searchGateway(Inet4Address ip) {
		return this.passerelles.indexOf(ip);
	}
	
	public static void main(String[] args) {
		new Server();
	}
}
