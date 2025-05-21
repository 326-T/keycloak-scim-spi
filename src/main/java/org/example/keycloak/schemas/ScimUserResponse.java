package org.example.keycloak.schemas;

import java.util.List;
import org.keycloak.models.KeycloakUriInfo;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

public record ScimUserResponse(
    List<String> schemas,

    ScimUserName name,
    List<ScimUserEmail> emails,
    String userName,
    String id,
    boolean active,
    ScimUserMeta meta
) {

  public ScimUserResponse(
      UserModel user,
      RealmModel realm,
      KeycloakUriInfo uriInfo
  ) {
    this(
        List.of("urn:ietf:params:scim:schemas:core:2.0:User"),
        new ScimUserName(user.getLastName(), user.getFirstName()),
        List.of(new ScimUserEmail(user.getEmail(), true)),
        user.getUsername(),
        user.getId(),
        user.isEnabled(),
        new ScimUserMeta(
            "User",
            uriInfo.getBaseUriBuilder()
                .path("realms")
                .path(realm.getName())
                .path("scim/v2/Users")
                .path(user.getId())
                .build().toString()
        )
    );
  }

  public record ScimUserName(
      String familyName,
      String givenName
  ) {

  }

  public record ScimUserEmail(
      String value,
      boolean primary
  ) {

  }

  public record ScimUserMeta(
      String resourceType,
      String location
  ) {

  }
}
