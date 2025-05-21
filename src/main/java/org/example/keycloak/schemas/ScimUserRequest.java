package org.example.keycloak.schemas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScimUserRequest(
    @JsonProperty("schemas")
    List<String> schemas,
    @JsonProperty("externalId")
    String externalId,
    @JsonProperty("name")
    ScimUserName name,
    @JsonProperty("emails")
    List<ScimUserEmail> emails,
    @JsonProperty("userName")
    String userName,
    @JsonProperty("active")
    boolean active,
    /** TODO: XHEXに対応する */
    @JsonProperty("displayName")
    String displayName
) {

  public record ScimUserName(
      @JsonProperty("formatted")
      String formatted,
      @JsonProperty("familyName")
      String familyName,
      @JsonProperty("givenName")
      String givenName
  ) {

  }

  public record ScimUserEmail(
      @JsonProperty("value")
      String value,
      @JsonProperty("type")
      String type,
      @JsonProperty("primary")
      boolean primary
  ) {

  }

  public record ScimUserMeta(
      @JsonProperty("resourceType")
      String resourceType
  ) {

  }
}
