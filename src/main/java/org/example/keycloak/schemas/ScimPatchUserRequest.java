package org.example.keycloak.schemas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScimPatchUserRequest(
    List<String> schemas,
    @JsonProperty("Operations")
    List<Operation> operations
) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Operation(
      String op,
      String path,
      String value
  ) {

  }
}
