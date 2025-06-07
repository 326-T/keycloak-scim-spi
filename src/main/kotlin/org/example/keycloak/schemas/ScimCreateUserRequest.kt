package org.example.keycloak.schemas

import com.fasterxml.jackson.annotation.JsonIgnoreProperties

@JsonIgnoreProperties(ignoreUnknown = true)
data class ScimCreateUserRequest(
    val schemas: List<String>,
    val externalId: String?,
    val name: ScimUserName,
    val emails: List<ScimUserEmail>,
    val userName: String,
    val active: Boolean
) {
    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ScimUserName(
        val familyName: String?,
        val givenName: String?
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ScimUserEmail(
        val value: String?,
        val type: String?,
        val primary: Boolean
    )

    @JsonIgnoreProperties(ignoreUnknown = true)
    data class ScimUserMeta(
        val resourceType: String?
    )
}