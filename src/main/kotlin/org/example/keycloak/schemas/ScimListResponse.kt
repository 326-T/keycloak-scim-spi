package org.example.keycloak.schemas

import com.fasterxml.jackson.annotation.JsonProperty

data class ScimListResponse<T>(
    val schemas: List<String>,
    val totalResults: Int,
    val startIndex: Int,
    val itemsPerPage: Int,
    @JsonProperty("Resources")
    val resources: List<T>
)