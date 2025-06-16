package test;

import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;

//import client.Client;
import serveur.Server;

public class ServerTest {

	public static void main(String[] args) {
		
		//init pour test
		
		Inet4Address finalAddIP;
		try {
		    finalAddIP = (Inet4Address) InetAddress.getByName("127.0.0.1");
		} catch (UnknownHostException e) {
		    e.printStackTrace();
		    return;
		}

		Inet4Address addIP = finalAddIP; //supprime et tu vas comprendre le problème
		
		//création du server 1
		new Thread(() -> {
		    new Server("S01",
		        9081,
		        new ArrayList<String>(Arrays.asList("S01")),
		        new ArrayList<Inet4Address>(Arrays.asList(addIP)),
		        new ArrayList<ArrayList<String>>(Arrays.asList(
	        		new ArrayList<String>()
		        )),
		        new ArrayList<Integer>(Arrays.asList(0))
		    );
		}).start();

		try {
		    Thread.sleep(3000);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		
		//création du server 2
		new Thread(() -> {
		    new Server("S02",
		        9082,
		        new ArrayList<String>(Arrays.asList("S02", "S01")),
		        new ArrayList<Inet4Address>(Arrays.asList(addIP, addIP)),
		        new ArrayList<ArrayList<String>>(Arrays.asList(
		        	new ArrayList<String>(),
		        	new ArrayList<String>()
		        )),
		        new ArrayList<Integer>(Arrays.asList(0, 1))
		    );
		}).start();
		
		try {
		    Thread.sleep(3000);
		} catch (InterruptedException e) {
		    e.printStackTrace();
		}
		
		//création du server 3
		new Thread(() -> {
		    new Server("S03",
		        9083,
		        new ArrayList<String>(Arrays.asList("S03", "S01")),
		        new ArrayList<Inet4Address>(Arrays.asList(addIP, addIP)),
		        new ArrayList<ArrayList<String>>(Arrays.asList(
		        	new ArrayList<String>(),
		        	new ArrayList<String>()
		        )),
		        new ArrayList<Integer>(Arrays.asList(0, 1))
		    );
		}).start();
		
//		new Thread(() -> {
//		    new Client("C1", 9081, "S01", addIP, 0); // Client récepteur
//		}).start();
//
//		new Thread(() -> {
//		    new Client("C2", 9082, "S02", addIP, 1); // Client émetteur (envoyer à C1)
//		}).start();
		
	}
}