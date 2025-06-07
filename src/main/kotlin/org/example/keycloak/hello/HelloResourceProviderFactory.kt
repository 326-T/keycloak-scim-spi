package org.example.keycloak.hello

import org.keycloak.Config
import org.keycloak.models.KeycloakSession
import org.keycloak.models.KeycloakSessionFactory
import org.keycloak.services.resource.RealmResourceProvider
import org.keycloak.services.resource.RealmResourceProviderFactory

class HelloResourceProviderFactory : RealmResourceProviderFactory {
    companion object {
        const val ID = "hello"
    }

    override fun getId(): String = ID

    override fun create(session: KeycloakSession): RealmResourceProvider =
        HelloResourceProvider(session)

    override fun init(config: Config.Scope) {}
    override fun postInit(factory: KeycloakSessionFactory) {}
    override fun close() {}
}