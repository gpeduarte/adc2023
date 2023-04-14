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
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.webapp.util.AuthToken;

@Path("/logout")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class LogoutResource {

	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	public LogoutResource() {}
	
	@DELETE
	@Path("/")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doLogout(AuthToken token) {
		
		LOG.info("User " + token.username + " logging out.");
		
		Key tokenKey = datastore.newKeyFactory().setKind("Tokens").newKey(token.tokenID);
		
		Transaction txn = datastore.newTransaction();
		
		try {
			
			Entity userToken = txn.get(tokenKey);
			
			if(userToken == null) {
				LOG.severe("Token non-existent.");
				return Response.status(Status.BAD_REQUEST).build();
			}
			
			txn.delete(tokenKey);
			
			txn.commit();
			
			LOG.info("User logged out successfully.");
			
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
