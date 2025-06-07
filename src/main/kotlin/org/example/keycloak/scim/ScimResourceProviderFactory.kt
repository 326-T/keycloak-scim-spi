package org.example.keycloak.scim

import org.keycloak.Config
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory
import org.keycloak.services.resource.RealmResourceProvider
import org.keycloak.services.resource.RealmResourceProviderFactory

class ScimResourceProviderFactory : RealmResourceProviderFactory {
    companion object {
        const val ID = "scim"
    }

    override fun getId(): String = ID

    override fun create(session: KeycloakSession): RealmResourceProvider =
        ScimResourceProvider(session)

    override fun init(config: Config.Scope) {}
    override fun postInit(factory: KeycloakSessionFactory) {}
    override fun close() {}
}