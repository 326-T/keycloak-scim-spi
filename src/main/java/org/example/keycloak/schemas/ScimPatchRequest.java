package org.example.keycloak.schemas;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public record ScimPatchRequest(
    @JsonProperty("schemas")
    List<String> schemas,
    @JsonProperty("Operations")
    List<Operation> operations
) {

  @JsonIgnoreProperties(ignoreUnknown = true)
  public record Operation(
      @JsonProperty("op")
      String op,
      @JsonProperty("path")
      String path,
      @JsonProperty("value")
      String value
  ) {

  }
}
