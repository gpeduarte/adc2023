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

import com.google.cloud.datastore.Datastore;
import com.google.cloud.datastore.DatastoreOptions;
import com.google.cloud.datastore.Entity;
import com.google.cloud.datastore.Key;
import com.google.cloud.datastore.KeyFactory;
import com.google.cloud.datastore.Transaction;

import pt.unl.fct.di.apdc.webapp.util.ProfileData;

@Path("/profile")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ProfileFillingResource {
	
	private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
	
	private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
	
	private KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
	
	public ProfileFillingResource() {}
	
	@PUT
	@Path("/{username}")
	@Consumes(MediaType.APPLICATION_JSON)
	public Response profileFill(@PathParam("username") String username, ProfileData data) {
		
		LOG.info("User " + username + " attempted updating profile.");
		
		Key userKey = userKeyFactory.newKey(username);
		
		Key updaterKey = userKeyFactory.newKey(data.updater);
		
		Key tokenKey = datastore.newKeyFactory().setKind("Tokens").newKey(data.updater);
		
		Transaction txn = datastore.newTransaction();
		
		try {
			
			Entity user = txn.get(userKey);
			
			Entity updater = txn.get(updaterKey);
			
			if(user == null) {
				LOG.severe("User does not exist.");
				return Response.status(Status.BAD_REQUEST).build();
			}
			
			Entity userToken = txn.get(tokenKey);
			
			if((userToken.getLong("token_ed") != data.token.expirationData) || userToken.getLong("token_ed") < System.currentTimeMillis()) {
				LOG.warning("User token has expired.");
				return Response.status(Status.FORBIDDEN).build();
			}
			
			if(!userToken.getString("token_username").equals(data.token.username)) {
				LOG.severe("Token not accepted.");
				return Response.status(Status.FORBIDDEN).build();
			}
			
			if(getRoleId(user.getString("user_role")) > getRoleId(updater.getString("user_role"))){
				LOG.severe("User does not have permissions to update the profile.");
				return Response.status(Status.FORBIDDEN).build();		
			}
			
			String newProfileStatus = null;
			if(data.profileStatus != null)
				newProfileStatus = data.profileStatus;
			else
				newProfileStatus = user.getString("user_profile_status");
			
			String newPhoneNum = null;
			if(data.phoneNum != null)
				newPhoneNum = data.phoneNum;
			else
				newPhoneNum = user.getString("user_phone_num");

			String newMobilePhone = null;
			if(data.mobilePhone != null)
				newMobilePhone = data.mobilePhone;
			else
				newPhoneNum = user.getString("user_mobile_phone_num");
			
			String newOccupation = null;
			if(data.occupation != null)
				newOccupation = data.occupation;
			else
				newOccupation = user.getString("user_occupation");

			String newWorkingPlace = null;
			if(data.workingPlace != null)
				newWorkingPlace = data.workingPlace;
			else
				newWorkingPlace = user.getString("user_working_place");

			String newAddress = null;
			if(data.address != null)
				newAddress = data.address;
			else
				newAddress = user.getString("user_address");

			String newCity = null;
			if(data.city != null)
				newCity = data.city;
			else
				newCity = user.getString("user_city");

			String newCP = null;
			if(data.cp != null)
				newCP = data.cp;
			else
				newCP = user.getString("user_cp");

			String newNIF = null;
			if(data.nif != null)
				newNIF = data.nif;
			else
				newNIF = user.getString("user_nif");
				
			user = Entity.newBuilder(userKey)
					.set("user_username", user.getString("user_username"))
					.set("user_email", user.getString("user_email"))
					.set("user_name", user.getString("user_name"))
					.set("user_pwd", user.getString("user_pwd"))
					.set("user_creation_time", user.getTimestamp("user_creation_time"))
					.set("user_role", user.getString("user_role"))
					.set("user_isActive", user.getBoolean("user_isActive"))
					.set("user_profile_status", newProfileStatus)
					.set("user_phone_num", newPhoneNum)
					.set("user_mobile_phone_num", newMobilePhone)
					.set("user_occupation", newOccupation)
					.set("user_working_place", newWorkingPlace)
					.set("user_address", newAddress)
					.set("user_city", newCity)
					.set("user_cp", newCP)
					.set("user_nif", newNIF)
					.build();
			
			txn.update(user);
			
			txn.commit();
			
			LOG.info("User updated profile successfully");
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
