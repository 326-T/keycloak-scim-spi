package org.example.keycloak.schemas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScimCreateUserRequest(
    List<String> schemas,
    String externalId,
    ScimUserName name,
    List<ScimUserEmail> emails,
    String userName,
    boolean active
) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ScimUserName(
      String familyName,
      String givenName
  ) {

  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ScimUserEmail(
      String value,
      String type,
      boolean primary
  ) {

  }

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record ScimUserMeta(
      String resourceType
  ) {

  }
}
