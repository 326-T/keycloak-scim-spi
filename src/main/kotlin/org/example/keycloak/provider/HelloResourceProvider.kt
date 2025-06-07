package org.example.keycloak.provider

import jakarta.ws.rs.GET
import jakarta.ws.rs.Produces
import jakarta.ws.rs.core.MediaType
import org.keycloak.models.KeycloakSession
import org.keycloak.services.resource.RealmResourceProvider

class HelloResourceProvider(private val session: KeycloakSession) : RealmResourceProvider {

    override fun getResource(): Any = this

    override fun close() {
        // No resources to close
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    fun hello(): Map<String, String> = mapOf("message" to "Hello, World!")
}