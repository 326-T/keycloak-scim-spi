package org.example.keycloak.schemas;

import java.time.OffsetDateTime;
import java.util.List;
import org.keycloak.models.UserModel;

public record ScimUserResponse(
    List<String> schemas,

    ScimUserName name,
    List<ScimUserEmail> emails,
    String userName,
    String id,
    boolean active
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
        user.isEnabled()
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
      OffsetDateTime created,
      OffsetDateTime lastModified,
      String location
  ) {

  }
}
