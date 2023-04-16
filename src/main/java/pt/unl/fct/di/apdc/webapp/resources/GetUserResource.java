package pt.unl.fct.di.apdc.webapp.resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import pt.unl.fct.di.apdc.webapp.util.GetData;
import pt.unl.fct.di.apdc.webapp.util.ListingData;
import pt.unl.fct.di.apdc.webapp.util.NotUserData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.logging.Logger;

@Path("/getuser")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class GetUserResource {

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");
    private final Gson g = new Gson();

    public GetUserResource(){}

    @GET
    @Path("/")
    public Response getUser(@QueryParam("username") String username){

        Key userKey = userKeyFactory.newKey(username);

        Transaction txn = datastore.newTransaction();

        try{

            Entity usr = txn.get(userKey);

            ListingData data = new GetData(usr.getString("user_username"), usr.getString("user_email"),
                    usr.getString("user_name"), usr.getString("user_role"), usr.getBoolean("user_isActive"),
                    usr.getString("user_profile_status"), usr.getString("user_phone_num"), usr.getString("user_mobile_phone_num"),
                    usr.getString("user_occupation"), usr.getString("user_working_place"), usr.getString("user_address"),
                    usr.getString("user_city"), usr.getString("user_cp"), usr.getString("user_nif"));

            txn.commit();

            return Response.ok(g.toJson(data)).build();

        }catch (Exception e) {
            txn.rollback();
            LOG.severe(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).entity(e.getMessage()).build();
        } finally {
            if(txn.isActive()) {
                txn.rollback();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }

    }

}
