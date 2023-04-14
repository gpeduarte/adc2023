package pt.unl.fct.di.apdc.webapp.util;

public class PasswordData {
	
	public AuthToken token;
	
	public String oldPassword;
	public String newPassword;
	
	public PasswordData(String oldPassword, String newPassword, AuthToken token) {
		this.oldPassword = oldPassword;
		this.newPassword = newPassword;
		this.token = token;
	}

}
