package pt.unl.fct.di.apdc.webapp.util;

public class DeletionData {
	
	public String delUsername;
	
	public String username;
	
	public AuthToken token;
	
	public DeletionData() {}
	
	public DeletionData(String delUsername, String username, AuthToken token) {
		this.delUsername = delUsername;
		this.username = username;
		this.token = token;
	}

}
