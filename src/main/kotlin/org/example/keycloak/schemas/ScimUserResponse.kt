package org.example.keycloak.schemas

import org.keycloak.models.KeycloakUriInfo
import org.keycloak.models.RealmModel
import org.keycloak.models.UserModel

data class ScimUserResponse(
    val schemas: List<String>,
    val name: ScimUserName,
    val emails: List<ScimUserEmail>,
    val userName: String,
    val id: String,
    val active: Boolean,
    val meta: ScimUserMeta
) {
    constructor(
        user: UserModel,
        realm: RealmModel,
        uriInfo: KeycloakUriInfo
    ) : this(
        schemas = listOf("urn:ietf:params:scim:schemas:core:2.0:User"),
        name = ScimUserName(user.lastName, user.firstName),
        emails = listOf(ScimUserEmail(user.email, true)),
        userName = user.username,
        id = user.id,
        active = user.isEnabled,
        meta = ScimUserMeta(
            resourceType = "User",
            location = uriInfo.baseUriBuilder
                .path("realms")
                .path(realm.name)
                .path("scim/v2/Users")
                .path(user.id)
                .build()
                .toString()
        )
    )

    data class ScimUserName(
        val familyName: String?,
        val givenName: String?
    )

    data class ScimUserEmail(
        val value: String?,
        val primary: Boolean
    )

    data class ScimUserMeta(
        val resourceType: String,
        val location: String
    )
}