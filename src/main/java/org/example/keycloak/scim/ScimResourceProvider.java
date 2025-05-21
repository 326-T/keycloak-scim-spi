package org.example.keycloak.scim;

import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PATCH;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;
import java.util.Objects;
import org.example.keycloak.schemas.ScimListResponse;
import org.example.keycloak.schemas.ScimPatchUserRequest;
import org.example.keycloak.schemas.ScimCreateUserRequest;
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
    // No resources to close
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
        users.stream().map(ScimUserResponse::new).toList()
    );
    return Response.ok(response).build();
  }

  @GET
  @Path("v2/Users/{userId}")
  @Produces(MediaType.APPLICATION_JSON)
  public Response getUser(@PathParam("userId") String userId) {

    RealmModel realm = session.getContext().getRealm();
    UserModel user = session.users().getUserById(realm, userId);

    ScimUserResponse response = new ScimUserResponse(user);
    return Response.ok(response).build();
  }

  @POST
  @Path("v2/Users")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response createUser(ScimCreateUserRequest request) {
    RealmModel realm = session.getContext().getRealm();
    UserModel user = session.users().addUser(realm, request.userName());
    user.setFirstName(request.name().givenName());
    user.setLastName(request.name().familyName());
    user.setEmail(request.emails().getFirst().value());
    user.setEnabled(request.active());

    ScimUserResponse response = new ScimUserResponse(user);
    return Response.status(Response.Status.CREATED).entity(response).build();
  }

  @PATCH
  @Path("v2/Users/{userId}")
  @Consumes(MediaType.APPLICATION_JSON)
  @Produces(MediaType.APPLICATION_JSON)
  public Response patchUser(
      @PathParam("userId") String userId,
      ScimPatchUserRequest request) {
    RealmModel realm = session.getContext().getRealm();
    UserModel user = session.users().getUserById(realm, userId);
    if (user == null) {
      return Response.status(Response.Status.NOT_FOUND).build();
    }
    for (ScimPatchUserRequest.Operation operation : request.operations()) {
      if (Objects.equals(operation.op(), "Add") || Objects.equals(operation.op(), "Replace")) {
        switch (operation.path()) {
          case "name.givenName" -> user.setFirstName(operation.value());
          case "name.familyName" -> user.setLastName(operation.value());
          case "emails[type eq \"work\"].value" -> user.setEmail(operation.value());
          case "active" -> user.setEnabled(Boolean.parseBoolean(operation.value()));
          default -> {
            // Ignore unsupported operations
          }
        }
      }
      if (Objects.equals(operation.op(), "Remove")) {
        switch (operation.path()) {
          case "name.givenName" -> user.setFirstName(null);
          case "name.familyName" -> user.setLastName(null);
          case "emails[type eq \"work\"].value" -> user.setEmail(null);
          case "active" -> user.setEnabled(false);
          default -> {
            // Ignore unsupported operations
          }
        }
      }
    }

    ScimUserResponse response = new ScimUserResponse(user);
    return Response.status(Response.Status.OK).entity(response).build();
  }
}
