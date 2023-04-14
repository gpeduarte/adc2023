package pt.unl.fct.di.apdc.webapp.resources;

import java.util.logging.Logger;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import com.google.appengine.repackaged.org.apache.commons.codec.digest.DigestUtils;
import com.google.cloud.Timestamp;
import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.webapp.util.RegisterData;

@Path("/register")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class RegisterResource {
	
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	public RegisterResource() {}
	
	@POST
	@Path("/userregister")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response doRegisterUser(RegisterData data) {
		LOG.fine("Creating user: " + data.username);
		
		if(!data.validRegistration() ) {
			LOG.severe("Wrong or missing parameters.");
			return Response.status(Status.BAD_REQUEST).build();
		}
		
		Transaction txn = datastore.newTransaction();
		try {
			Key userKey = datastore.newKeyFactory().setKind("User").newKey(data.username);
			
			Entity user = txn.get(userKey);
			
			String role = "user";
			
			if(data.username.equals("super_user"))
				role = "su";
			
			if(user != null) {
				txn.rollback();
				return Response.status(Status.BAD_REQUEST).entity("User already exists.").build();
			} else {
				user = Entity.newBuilder(userKey)
						.set("user_username", data.username)
						.set("user_name", data.name)
						.set("user_pwd", DigestUtils.sha3_512Hex(data.password))
						.set("user_email", data.email)
						.set("user_creation_time", Timestamp.now())
						.set("user_role", role)
						.set("user_isActive", false)
						.set("user_profile_status", data.profileStatus)
						.set("user_phone_num", data.phoneNum)
						.set("user_mobile_phone_num", data.mobilePhone)
						.set("user_occupation", data.occupation)
						.set("user_working_place", data.workingPlace)
						.set("user_address", data.address)
						.set("user_city", data.city)
						.set("user_cp", data.cp)
						.set("user_nif", data.nif)
						.build();
				
				txn.add(user);
				txn.commit();
				
				LOG.info("User registered " + data.username);
				return Response.ok().build();
			}
		} finally {
			if(txn.isActive()) {
				txn.rollback();
				LOG.warning("Failed to register user: " + data.username);
				return Response.status(Status.INTERNAL_SERVER_ERROR).build();
			}
		}
	}
}
