package org.example.keycloak.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ScimListResponse<T>(

    @JsonProperty("schemas")
    List<String> schemas,
    @JsonProperty("totalResults")
    int totalResults,
    @JsonProperty("startIndex")
    int startIndex,
    @JsonProperty("itemsPerPage")
    int itemsPerPage,
    @JsonProperty("Resources")
    List<T> resources

) {

}
