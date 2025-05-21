package org.example.keycloak.schemas;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

public record ScimListResponse<T>(

    List<String> schemas,
    int totalResults,
    int startIndex,
    int itemsPerPage,
    @JsonProperty("Resources")
    List<T> resources

) {

}
