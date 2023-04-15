package pt.unl.fct.di.apdc.webapp.resources;

import com.google.cloud.datastore.*;
import com.google.gson.Gson;
import pt.unl.fct.di.apdc.webapp.util.AuthToken;
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
    public Response listUsers(@QueryParam("token") AuthToken token){

        LOG.info("Listing users for user: " + token.username);

        Key requesterKey = userKeyFactory.newKey(token.username);
        Key tokenKey = datastore.newKeyFactory().setKind("Tokens").newKey(token.username);

        Transaction txn = datastore.newTransaction();

        try{

            Entity userToken = txn.get(tokenKey);

            if((userToken.getLong("token_ed") != token.expirationData) || userToken.getLong("token_ed") < System.currentTimeMillis()) {
                LOG.warning("User token has expired.");
                return Response.status(Response.Status.FORBIDDEN).build();
            }

            if(!userToken.getString("token_username").equals(token.username)) {
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
                        .setFilter(
                                StructuredQuery.CompositeFilter.and(
                                    StructuredQuery.CompositeFilter.and(
                                            StructuredQuery.PropertyFilter.eq("user_role", "user")
                                            , StructuredQuery.PropertyFilter.eq("user_isActive", true)
                                    )
                                    , StructuredQuery.PropertyFilter.eq("user_profile_status", "public")
                                )
                        )
                        .build();
                    QueryResults<Entity> usersUser = datastore.run(queryUser);
                    while(usersUser.hasNext()) {
                        Entity usr = usersUser.next();
                        UserData data = new UserData(usr.getString("user_username"), usr.getString("user_email"), usr.getString("user_name"));
                        usersToList.add(data);
                    }
                    break;
                case "gbo":
                    Query<Entity> queryGBO = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .setFilter(
                                    StructuredQuery.PropertyFilter.eq("user_role", "user")
                            )
                            .build();
                    QueryResults<Entity> usersGBO = datastore.run(queryGBO);
                    addUsersData(usersToList, usersGBO);
                    break;
                case "gs":
                    Query<Entity> queryGS = Query.newEntityQueryBuilder()
                        .setKind("User")
                        .setFilter(
                                StructuredQuery.PropertyFilter.eq("user_role", "user")
                        )
                        .build();
                    QueryResults<Entity> usersGS = datastore.run(queryGS);
                    addUsersData(usersToList, usersGS);
                    queryGS = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .setFilter(
                                    StructuredQuery.PropertyFilter.eq("user_role", "user")
                            )
                            .build();
                    usersGS = datastore.run(queryGS);
                    addUsersData(usersToList, usersGS);
                    break;
                case "su":
                    Query<Entity> querySU = Query.newEntityQueryBuilder()
                            .setKind("User")
                            .build();
                    QueryResults<Entity> usersSU = datastore.run(querySU);
                    addUsersData(usersToList, usersSU);
                    break;
            }

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

    private void addUsersData(List<ListingData> usersToList, QueryResults<Entity> usersGS) {
        while(usersGS.hasNext()) {
            Entity usr = usersGS.next();
            NotUserData data = new NotUserData(usr.getString("user_username"), usr.getString("user_email"),
                    usr.getString("user_name"), usr.getString("user_pwd"), usr.getBoolean("user_isActive"),
                    usr.getString("user_profile_status"), usr.getString("user_phone_num"), usr.getString("user_mobile_phone_num"),
                    usr.getString("user_occupation"), usr.getString("user_working_place"), usr.getString("user_address"),
                    usr.getString("user_city"), usr.getString("user_cp"), usr.getString("user_nif"));
            usersToList.add(data);
        }
    }

}
