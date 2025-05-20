package org.example.keycloak.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.time.OffsetDateTime;
import java.util.List;

public record ScimUserResponse(
    @JsonProperty("schemas")
    List<String> schemas,

    @JsonProperty("name")
    ScimUserName name,
    @JsonProperty("emails")
    List<ScimUserEmail> emails,
    @JsonProperty("userName")
    String userName,
    @JsonProperty("id")
    String id,
    @JsonProperty("active")
    boolean active
) {

  public record ScimUserName(
      @JsonProperty("familyName")
      String familyName,
      @JsonProperty("givenName")
      String givenName
  ) {

  }

  public record ScimUserEmail(
      @JsonProperty("value")
      String value,
      @JsonProperty("primary")
      boolean primary
  ) {

  }

  public record ScimUserMeta(
      @JsonProperty("resourceType")
      String resourceType,
      @JsonProperty("created")
      OffsetDateTime created,
      @JsonProperty("lastModified")
      OffsetDateTime lastModified,
      @JsonProperty("location")
      String location
  ) {

  }
}
