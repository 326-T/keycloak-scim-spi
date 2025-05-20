package org.example.keycloak.scim;

import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import org.example.keycloak.schemas.ScimListResponse;
import org.example.keycloak.schemas.ScimUserResponse;
import org.example.keycloak.util.ScimFilterUtil;
import org.keycloak.models.KeycloakSession;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;
import org.keycloak.services.resource.RealmResourceProvider;

public class ScimResourceProvider implements RealmResourceProvider {

  private final KeycloakSession session;

  public ScimResourceProvider(KeycloakSession session) {
    this.session = session;
  }

  @Override
  public Object getResource() {
    return this;
  }

  @Override
  public void close() {
  }

  @GET
  @Path("v2/Users")
  @Produces(MediaType.APPLICATION_JSON)
  public Response listUsers(
      @QueryParam("filter") String filter,
      @QueryParam("startIndex") @DefaultValue("0") int startIndex,
      @QueryParam("itemsPerPage") @DefaultValue("100") int itemsPerPage) {

    RealmModel realm = session.getContext().getRealm();
    int count = session.users().getUsersCount(realm, ScimFilterUtil.parse(filter));
    List<UserModel> users = session.users().searchForUserStream(
        realm, ScimFilterUtil.parse(filter), startIndex, itemsPerPage
    ).toList();

    ScimListResponse<ScimUserResponse> response = new ScimListResponse<>(
        List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse"),
        count,
        startIndex,
        itemsPerPage,
        users.stream().map(user -> new ScimUserResponse(
            List.of("urn:ietf:params:scim:schemas:core:2.0:User"),
            new ScimUserResponse.ScimUserName(user.getLastName(), user.getFirstName()),
            List.of(new ScimUserResponse.ScimUserEmail(user.getEmail(), true)),
            user.getUsername(),
            user.getId(),
            user.isEnabled()
        )).toList()
    );
    return Response.ok(response).build();
  }

  @GET
  @Path("v2/Users/{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUser(@PathParam("userId") String userId) {

    RealmModel realm = session.getContext().getRealm();
    UserModel user = session.users().getUserById(realm, userId);

    ScimUserResponse response = new ScimUserResponse(
        List.of("urn:ietf:params:scim:schemas:core:2.0:User"),
        new ScimUserResponse.ScimUserName(user.getLastName(), user.getFirstName()),
        List.of(new ScimUserResponse.ScimUserEmail(user.getEmail(), true)),
        user.getUsername(),
        user.getId(),
        user.isEnabled()
    );
    return Response.ok(response).build();
  }
}
