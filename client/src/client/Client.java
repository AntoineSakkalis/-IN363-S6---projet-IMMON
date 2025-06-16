package client;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Scanner;

import common.Link;
import common.Trame;
import common.Trame_connect;
import common.Trame_getClients;
import common.Trame_message;

public class Client {

    private String name = "C1";
    private int port = 9081;
    private String serverToConnect = "S01";
    private Inet4Address ipGateway;
    private Link link;
    private volatile boolean running = true;

    public Client(String name, int port, String serverToConnect, Inet4Address ipGateway) {
        this.name = name;
        this.port = port;
        this.serverToConnect = serverToConnect;
        this.ipGateway = ipGateway;
    
        runClient();
    }

    public Client() {
    	
        Scanner scanner = new Scanner(System.in);
    	System.out.println("Choisissez votre nom sur le réseau");
    	this.name = scanner.nextLine();
    	
        try {
            ipGateway = (Inet4Address) InetAddress.getByName("127.0.0.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }

    	
        runClient();
    }

    public void runClient() {
        try {
            this.link = new Link(ipGateway, port);

            // Connect to server
            Trame_connect connect = new Trame_connect(serverToConnect, serverToConnect, name, false);
            this.link.send(connect);
            connect = (Trame_connect) link.receive();

            if (!connect.isApproval()) {
                System.out.println("Connexion au serveur impossible");
                this.link.close();
                return;
            }
            System.out.println("Serveur connecté !");

            // Start receiving thread
            Thread receiverThread = new Thread(this::receiveLoop);
            receiverThread.start();

            // Start console menu & input handling
            menuLoop();

            // After exiting menu, close connection and stop receiver thread
            running = false;
            receiverThread.join();
            link.close();

        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    // Thread method for receiving messages continuously
    private void receiveLoop() {
        while (running) {
            try {
                Trame trame = link.receive();
                if (trame == null) continue;

                if (trame instanceof Trame_message) {
                    Trame_message msg = (Trame_message) trame;
                    // Print incoming message only if for this client
                    if (name.equals(msg.getClient_cible())) {
                        System.out.println("\n[Message de la part de " + msg.getClient_source() + "]: " + msg.getDu());
                        System.out.print(">> "); // reprint prompt
                    }
                } else if (trame instanceof Trame_getClients) {
                    Trame_getClients listTrame = (Trame_getClients) trame;
                    System.out.println("\n[Liste des clients]: " + listTrame.getListCli());
                    System.out.print(">> ");
                } else {
                    // Autres trames !!
                }
            } catch (IOException e) {
                if (running) {
                    System.out.println("Connexion perdue");
                    running = false;
                }
            }
        }
    }

    // Console menu and input loop
    private void menuLoop() {
        Scanner scanner = new Scanner(System.in);

        while (running) {
            System.out.println("\n--- Menu ---");
            System.out.println("1. Liste des utilisateurs");
            System.out.println("2. Démarrer une conversation avec un utilisateur spécifique");
            System.out.println("0. Arrêter");
            System.out.print("Choisissez une option: ");
            String option = scanner.nextLine();

            switch (option) {
                case "1":
                    requestClientList();
                    break;

                case "2":
                    startChat(scanner);
                    break;
                    
                case "0":
                    running = false;
                    break;

                default:
                    System.out.println("Option invalide");
                    break;
            }
        }
        scanner.close();
    }

    private void requestClientList() {
        try {
            Trame_getClients getClients = new Trame_getClients(null, this.serverToConnect, this.name, new ArrayList<String>());
            link.send(getClients);
        } catch (IOException e) {
            System.out.println("échec de récupération des clients " + e.getMessage());
        }
    }

    private void startChat(Scanner scanner) {
        System.out.print("Entrez le nom de votre correspondant: ");
        String chatClient = scanner.nextLine().trim();

        if (chatClient.isEmpty()) {
            System.out.println("Le nom du client ne peut pas être vide");
            return;
        }

        System.out.println("Début de la conversation avec " + chatClient + ". Faites /exit pour retourner au menu.");

        while (running) {
            System.out.print(">> ");
            String message = scanner.nextLine();

            if (message.equalsIgnoreCase("/exit")) {
                System.out.println("Fin de la conversation...");
                break;
            } else if (message.equalsIgnoreCase("/getclients")) {
                requestClientList();
                continue;
            }

            // Send chat message
            try {
                Trame_message trameMessage = new Trame_message(
                    null,
                    this.serverToConnect,
                    chatClient,
                    this.name,
                    message
                );
                link.send(trameMessage);
            } catch (IOException e) {
                System.out.println("échec d'envoi du message : " + e.getMessage());
                break;
            }
        }
    }

    public static void main(String[] args) {
        new Client();
    }
}