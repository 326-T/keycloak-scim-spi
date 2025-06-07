package org.example.keycloak.config

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.registerKotlinModule
import jakarta.ws.rs.ext.ContextResolver
import jakarta.ws.rs.ext.Provider

@Provider
class JacksonConfig : ContextResolver<ObjectMapper> {
    private val objectMapper = ObjectMapper().registerKotlinModule()

    override fun getContext(type: Class<*>?): ObjectMapper {
        return objectMapper
    }
}