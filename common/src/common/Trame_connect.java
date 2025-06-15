package common;

public class Trame_connect extends Trame{
	
	private static final long serialVersionUID = 7882078727879685850L;
	private String client;
	private boolean approval; //true : OK, false : problème
	
	public Trame_connect(String serveur_cible, String serveur_source, String client, boolean approval) {
		super(3, serveur_cible, serveur_source);
		this.setClient(client);
		this.setApproval(approval);
	}

	public String getClient() {
		return client;
	}

	public void setClient(String client) {
		this.client = client;
	}

	public boolean isApproval() {
		return approval;
	}

	public void setApproval(boolean approval) {
		this.approval = approval;
	}

	
	
	
}