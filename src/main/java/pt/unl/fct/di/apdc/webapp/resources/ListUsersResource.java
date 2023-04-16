package pt.unl.fct.di.apdc.webapp.resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import pt.unl.fct.di.apdc.webapp.util.NotUserData;
import pt.unl.fct.di.apdc.webapp.util.ListingData;
import pt.unl.fct.di.apdc.webapp.util.UserData;

import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Path("/listusers")
@Produces(MediaType.APPLICATION_JSON + ";charset=utf-8")
public class ListUsersResource {

    private static final Logger LOG = Logger.getLogger(LoginResource.class.getName());
    private final Datastore datastore = DatastoreOptions.getDefaultInstance().getService();
    private final Gson g = new Gson();
    private KeyFactory userKeyFactory = datastore.newKeyFactory().setKind("User");

    public ListUsersResource(){}

    @GET
    @Path("/")
    @Consumes(MediaType.APPLICATION_JSON)
    public Response listUsers(@QueryParam("tokenUsername") String tokenUsername, @QueryParam("tokenID") String tokenID
            , @QueryParam("creationData") long creationData, @QueryParam("expirationData") long expirationData){

        LOG.info("Listing users for user: " + tokenUsername);

        Key requesterKey = userKeyFactory.newKey(tokenUsername);
        Key tokenKey = datastore.newKeyFactory().setKind("Tokens").newKey(tokenUsername);

        Transaction txn = datastore.newTransaction();

        try{

            Entity userToken = txn.get(tokenKey);

            if((userToken.getLong("token_ed") != expirationData) || userToken.getLong("token_ed") < System.currentTimeMillis()) {
                LOG.warning("User token has expired.");
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            if(!userToken.getString("token_username").equals(tokenUsername)) {
                LOG.severe("Token not accepted.");
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            Entity requester = txn.get(requesterKey);

            String role = requester.getString("user_role");

            List<ListingData> usersToList = new ArrayList<>();

            switch(role){
                case "user":
                    Query<Entity> queryUser = Query.newEntityQueryBuilder()
                        .setKind("User")
                        .build();
                    QueryResults<Entity> usersUser = datastore.run(queryUser);
                    while(usersUser.hasNext()) {
                        Entity usr = usersUser.next();
                        if(usr.getString("user_profile_status").equals("public") && usr.getString("user_role").equals("user") && usr.getBoolean("user_isActive")) {
                            ListingData data = new UserData(usr.getString("user_username"), usr.getString("user_email"), usr.getString("user_name"));
                            usersToList.add(data);
                        }
                    }
                    break;
                case "gbo":
                    Query<Entity> queryGBO = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .build();
                    QueryResults<Entity> usersGBO = datastore.run(queryGBO);
                    while(usersGBO.hasNext()) {
                        Entity usr = usersGBO.next();
                        if(usr.getString("user_role").equals("user")) {
                            ListingData data = new NotUserData(usr.getString("user_username"), usr.getString("user_email"),
                                    usr.getString("user_name"), usr.getString("user_pwd"), usr.getString("user_role"), usr.getBoolean("user_isActive"),
                                    usr.getString("user_profile_status"), usr.getString("user_phone_num"), usr.getString("user_mobile_phone_num"),
                                    usr.getString("user_occupation"), usr.getString("user_working_place"), usr.getString("user_address"),
                                    usr.getString("user_city"), usr.getString("user_cp"), usr.getString("user_nif"));
                            usersToList.add(data);
                        }
                    }
                    break;
                case "gs":
                    Query<Entity> queryGS = Query.newEntityQueryBuilder()
                        .setKind("User")
                        .build();
                    QueryResults<Entity> usersGS = datastore.run(queryGS);
                    while(usersGS.hasNext()) {
                        Entity usr = usersGS.next();
                        if (usr.getString("user_role").equals("user") || usr.getString("user_role").equals("gbo")) {
                            ListingData data = new NotUserData(usr.getString("user_username"), usr.getString("user_email"),
                                    usr.getString("user_name"), usr.getString("user_pwd"), usr.getString("user_role"), usr.getBoolean("user_isActive"),
                                    usr.getString("user_profile_status"), usr.getString("user_phone_num"), usr.getString("user_mobile_phone_num"),
                                    usr.getString("user_occupation"), usr.getString("user_working_place"), usr.getString("user_address"),
                                    usr.getString("user_city"), usr.getString("user_cp"), usr.getString("user_nif"));
                            usersToList.add(data);
                        }
                    }
                    break;
                case "su":
                    Query<Entity> querySU = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .build();
                    QueryResults<Entity> usersSU = datastore.run(querySU);
                    while(usersSU.hasNext()) {
                        Entity usr = usersSU.next();
                        ListingData data = new NotUserData(usr.getString("user_username"), usr.getString("user_email"),
                                usr.getString("user_name"), usr.getString("user_pwd"), usr.getString("user_role"), usr.getBoolean("user_isActive"),
                                usr.getString("user_profile_status"), usr.getString("user_phone_num"), usr.getString("user_mobile_phone_num"),
                                usr.getString("user_occupation"), usr.getString("user_working_place"), usr.getString("user_address"),
                                usr.getString("user_city"), usr.getString("user_cp"), usr.getString("user_nif"));
                        usersToList.add(data);
                    }
                    break;
            }

            txn.commit();

            LOG.info("Listing users.");
            return Response.ok(g.toJson(usersToList)).build();

        }catch(Exception e) {
            LOG.severe(e.getMessage());
            return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
        }finally {
            if(txn.isActive()) {
                txn.rollback();
                return Response.status(Response.Status.INTERNAL_SERVER_ERROR).build();
            }
        }

    }

}
