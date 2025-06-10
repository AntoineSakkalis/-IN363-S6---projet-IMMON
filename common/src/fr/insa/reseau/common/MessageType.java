package fr.insa.reseau.common;

/** Codes de messages échangés dans toute la promo. */
public enum MessageType {
    /* Serveur ↔ serveur */
    CONNECT_SERVER,		//commence une connexion avec un autre server
    ROUTE_TABLE,	
    NEW_CLIENT, 		//annonce aux autres servers un nouveau client

    /* Client ↔ serveur */
    CONNECT_CLIENT,		//commence une connexion avec un server client au server

    /* Généraux */
    DATA,
    ACK,
    CORRUPTED,
    END,
    REQUEST_CLIENT_LIST,//demande la liste des clients disponible sur le réseau à un aure server
    IS_ALIVE			//demande si une autre entité (server ou client) est actif
}
