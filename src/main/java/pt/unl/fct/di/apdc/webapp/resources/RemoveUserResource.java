package pt.unl.fct.di.apdc.webapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.PathElement;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.webapp.util.AuthToken;
import pt.unl.fct.di.apdc.webapp.util.DeletionData;

@Path("/remove")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RemoveUserResource {
	
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	private KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	
	public RemoveUserResource() {}
	
	@DELETE
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
	public Response removeUser(DeletionData data, AuthToken token) {
		
		LOG.info("Deletion of user: " + data.delUsername + " by user: " + data.username);
		
		Key userKey = userKeyFactory.newKey(data.username);
		Key delUserKey = userKeyFactory.newKey(data.delUsername);
		Key delUserTokenKey = datastore.newKeyFactory().setKind("Tokens").newKey(data.delUsername);
		Key delUserStatsKey = datastore.newKeyFactory().addAncestors(PathElement.of("User", data.delUsername)).setKind("UserStats").newKey("counters");
		Key delUserLogKey = datastore.newKeyFactory().addAncestors(PathElement.of("User", data.delUsername)).setKind("UserLog").newKey("logs");
		
		Transaction txn = datastore.newTransaction();
		
		try {

			Entity user = txn.get(userKey);

			if(!user.getBoolean("user_isActive")) {
				LOG.severe("User cannot do actions.");
				return Response.status(Status.FORBIDDEN).build();
			}
			
			Entity userToken = txn.get(datastore.newKeyFactory().setKind("Tokens").newKey(data.username));
			
			if((userToken.getLong("token_ed") != token.expirationData) || userToken.getLong("token_ed") < System.currentTimeMillis()) {
				LOG.warning("User token has expired.");
				return Response.status(Status.FORBIDDEN).build();
			}
			
			if(!userToken.getString("token_username").equals(token.username)) {
				LOG.severe("Token not accepted.");
				return Response.status(Status.FORBIDDEN).build();
			}
			
			Entity delUser = txn.get(delUserKey);
			
			if(delUser == null) {
				LOG.severe("User " + data.delUsername + " does not exist.");
				return Response.status(Status.BAD_REQUEST).build();
			}
			
			if(this.getRoleId(delUser.getString("user_role")) > this.getRoleId(user.getString("user_role"))) {
				LOG.severe("User " + data.delUsername + " cannot be removed by user " + data.username);
				return Response.status(Status.FORBIDDEN).build();
			}
			
			txn.delete(delUserStatsKey);
			txn.delete(delUserLogKey);
			txn.delete(delUserTokenKey);
			txn.delete(delUserKey);
			
			txn.commit();
			
			
			LOG.info("User " + data.delUsername + " removed successfully");
			return Response.ok().build();
			
		}catch (Exception e) {
			txn.rollback();
			LOG.severe(e.getMessage());
			return Response.status(Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
		} finally {
			if(txn.isActive()) {
				txn.rollback();
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
	
	private int getRoleId(String role) {
		int roleId = -1;
		switch(role) {
			case "user": roleId = 0;
						break;
			case "gbo": roleId = 1;
						break;
			case "gs": roleId = 2;
						break;
			case "su": roleId = 3;
						break;
		}
		return roleId;
	}

}
