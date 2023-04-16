package pt.unl.fct.di.apdc.webapp.util;

import java.util.UUID;

public class AuthToken {

	public static final long EXPIRATION_TIME = 1000*60*60; //1h

	public String tokenUsername;

	public String role;

	public String tokenID;

	public long creationData;

	public long expirationData;

	public AuthToken(){}

	public AuthToken(String username, String role) {

		this.tokenUsername = username;

		this.role = role;

		this.tokenID = UUID.randomUUID().toString();

		this.creationData = System.currentTimeMillis();

		this.expirationData = this.creationData + AuthToken.EXPIRATION_TIME;

	}
}