package pt.unl.fct.di.apdc.webapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.commons.codec.digest.DigestUtils;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.webapp.util.AuthToken;
import pt.unl.fct.di.apdc.webapp.util.PasswordData;

@Path("/changepassword")
@Produces(MediaType.APPLICATION_JSON + "charset=utf-8")
public class PasswordChangeResource {
	
private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	private KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	
	public PasswordChangeResource() {}
	
	@PUT
	@Path("/{username}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response changePassword(@PathParam("username") String username, PasswordData passwords, AuthToken token) {
		
		LOG.info("User " + username + " attempt of changing password");
		
		Key userKey = userKeyFactory.newKey(username);
		Key tokenKey = datastore.newKeyFactory().setKind("Tokens").newKey(token.tokenID);
		
		Transaction txn = datastore.newTransaction();
		
		try {
			
			Entity userToken = txn.get(tokenKey);
			
			if((userToken.getLong("token_ed") != token.expirationData) || userToken.getLong("token_ed") < System.currentTimeMillis()) {
				LOG.warning("User token has expired.");
				return Response.status(Status.FORBIDDEN).build();
			}
			
			if(!userToken.getString("token_username").equals(token.username)) {
				LOG.severe("Token not accepted.");
				return Response.status(Status.FORBIDDEN).build();
			}
			
			Entity user = txn.get(userKey);
			
			if(user == null) {
				LOG.severe("User does not exist.");
				return Response.status(Status.BAD_REQUEST).build();
			}
			
			if(!username.equals(userToken.getString("token_username"))) {
				LOG.severe("User cannot change other user's password.");
				return Response.status(Status.FORBIDDEN).build();
			}
			
			String hashedPWD = (String) user.getString("user_pwd");
			if(hashedPWD.equals(DigestUtils.sha3_512Hex(passwords.oldPassword))) {
				
				user = Entity.newBuilder(userKey)
						.set("user_username", user.getString("user_username"))
						.set("user_email", user.getString("user_email"))
						.set("user_name", user.getString("user_name"))
						.set("user_pwd", user.getString("user_pwd"))
						.set("user_creation_time", user.getTimestamp("user_creation_time"))
						.set("user_role", user.getString("user_role"))
						.set("user_isActive", user.getBoolean("user_isActive"))
						.set("user_profile_status", user.getString("user_profile_status"))
						.set("user_phone_num", user.getString("user_phone_num"))
						.set("user_mobile_phone_num", user.getString("user_mobile_phone_num"))
						.set("user_occupation", user.getString("user_occupation"))
						.set("user_working_place", user.getString("user_working_place"))
						.set("user_address", user.getString("user_address"))
						.set("user_city", user.getString("user_city"))
						.set("user_cp", user.getString("user_cp"))
						.set("user_nif", user.getString("user_nif"))
						.build();
				
			}
			
			txn.update(user);
			
			txn.commit();
			
			LOG.info("Changed passwords successfully.");
			
			return Response.ok().build();
			
		}catch(Exception e) {
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).build();
		}finally {
			if(txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
		
	}

}
