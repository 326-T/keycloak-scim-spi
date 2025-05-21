package org.example.keycloak.schemas;

import java.time.OffsetDateTime;
import java.util.List;
import org.example.keycloak.schemas.ScimCreateUserRequest.ScimUserMeta;
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
      UserModel user
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
            "/Users/" + user.getId()
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
