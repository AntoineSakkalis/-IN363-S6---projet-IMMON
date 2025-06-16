package common;

import java.util.ArrayList;

public class Trame_getClients extends Trame {

    private String client_source;
    private ArrayList<String> listCli;  // list of clients

    public Trame_getClients(String serveur_cible, String serveur_source, String client_source, ArrayList<String> listCli) {
        super(4, serveur_cible, serveur_source);
        this.client_source = client_source;
        this.listCli = listCli;
    }

    public String getClient_source() {
        return client_source;
    }

    public ArrayList<String> getListCli() {
        return listCli;
    }
    
    public void setDu(ArrayList<String> listCli) {
        this.listCli = listCli;
    }
}