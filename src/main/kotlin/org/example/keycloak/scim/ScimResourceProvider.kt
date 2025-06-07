package org.example.keycloak.scim

import jakarta.ws.rs.*
import jakarta.ws.rs.core.MediaType
import jakarta.ws.rs.core.Response
import org.example.keycloak.schemas.ScimCreateUserRequest
import org.example.keycloak.schemas.ScimListResponse
import org.example.keycloak.schemas.ScimPatchUserRequest
import org.example.keycloak.schemas.ScimUserResponse
import org.example.keycloak.util.ScimFilterUtil
import org.keycloak.models.KeycloakSession
import org.keycloak.services.resource.RealmResourceProvider
import java.util.stream.Collectors

@Suppress("unused")
class ScimResourceProvider(private val session: KeycloakSession) : RealmResourceProvider {

    override fun getResource(): Any = this

    override fun close() {
        // No resources to close
    }

    @GET
    @Path("v2/Users")
    @Produces(MediaType.APPLICATION_JSON)
    fun listUsers(
        @QueryParam("filter") filter: String?,
        @QueryParam("startIndex") @DefaultValue("0") startIndex: Int,
        @QueryParam("itemsPerPage") @DefaultValue("100") itemsPerPage: Int
    ): Response {
        val realm = session.context.realm
        val filterMap = ScimFilterUtil.parse(filter)
        val count = session.users().getUsersCount(realm, filterMap)
        val users = session.users().searchForUserStream(realm, filterMap, startIndex, itemsPerPage)
            .collect(Collectors.toList())

        val response = ScimListResponse(
            schemas = listOf("urn:ietf:params:scim:api:messages:2.0:ListResponse"),
            totalResults = count,
            startIndex = startIndex,
            itemsPerPage = itemsPerPage,
            resources = users.map { user ->
                ScimUserResponse(user, realm, session.context.uri)
            }
        )
        return Response.ok(response).build()
    }

    @GET
    @Path("v2/Users/{userId}")
    @Produces(MediaType.APPLICATION_JSON)
    fun getUser(@PathParam("userId") userId: String): Response {
        val realm = session.context.realm
        val user = session.users().getUserById(realm, userId)

        val response = ScimUserResponse(user, realm, session.context.uri)
        return Response.ok(response).build()
    }

    @POST
    @Path("v2/Users")
    @Consumes(MediaType.APPLICATION_JSON, "application/scim+json")
    @Produces(MediaType.APPLICATION_JSON)
    fun createUser(request: ScimCreateUserRequest): Response {
        val realm = session.context.realm
        val user = session.users().addUser(realm, request.userName)
        
        user.firstName = request.name.givenName
        user.lastName = request.name.familyName
        user.email = request.emails.first().value
        user.isEmailVerified = true
        user.isEnabled = request.active

        val response = ScimUserResponse(user, realm, session.context.uri)
        return Response.status(Response.Status.CREATED).entity(response).build()
    }

    @PATCH
    @Path("v2/Users/{userId}")
    @Consumes(MediaType.APPLICATION_JSON, "application/scim+json")
    @Produces(MediaType.APPLICATION_JSON)
    fun patchUser(
        @PathParam("userId") userId: String,
        request: ScimPatchUserRequest
    ): Response {
        val realm = session.context.realm
        val user = session.users().getUserById(realm, userId)
            ?: return Response.status(Response.Status.NOT_FOUND).build()

        for (operation in request.operations) {
            when (operation.op) {
                "Add", "Replace" -> {
                    when (operation.path) {
                        "name.givenName" -> user.firstName = operation.value
                        "name.familyName" -> user.lastName = operation.value
                        "emails[type eq \"work\"].value" -> {
                            user.email = operation.value
                            user.isEmailVerified = true
                        }
                        "active" -> user.isEnabled = operation.value.toBoolean()
                        else -> {
                            // Ignore unsupported operations
                        }
                    }
                }
                "Remove" -> {
                    when (operation.path) {
                        "name.givenName" -> user.firstName = null
                        "name.familyName" -> user.lastName = null
                        "emails[type eq \"work\"].value" -> user.email = null
                        "active" -> user.isEnabled = false
                        else -> {
                            // Ignore unsupported operations
                        }
                    }
                }
            }
        }

        val response = ScimUserResponse(user, realm, session.context.uri)
        return Response.status(Response.Status.OK).entity(response).build()
    }
}