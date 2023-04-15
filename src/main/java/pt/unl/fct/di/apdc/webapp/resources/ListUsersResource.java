package pt.unl.fct.di.apdc.webapp.resources;

import com.google.cloud.datastore.*;
import com.google.datastore.v1.CompositeFilter;
import com.google.datastore.v1.PropertyFilter;
import com.google.gson.Gson;
import pt.unl.fct.di.apdc.webapp.util.AuthToken;
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
    public Response listUsers(AuthToken token){

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

            List<UserData> usersToList = new ArrayList<>();

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
                    while(usersGBO.hasNext()) {
                        Entity usr = usersGBO.next();
                        UserData data = new UserData(usr.getString("user_username"), usr.getString("user_email"), usr.getString("user_name"));
                        usersToList.add(data);
                    }
                    break;
                case "gs":break;
                case "su":break;
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

}
