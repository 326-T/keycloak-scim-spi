package org.example.keycloak.schemas

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScimPatchUserRequest(
    val schemas: List<String>,
    @JsonProperty("Operations")
    val operations: List<Operation>
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class Operation(
        val op: String,
        val path: String,
        val value: String
    )
}