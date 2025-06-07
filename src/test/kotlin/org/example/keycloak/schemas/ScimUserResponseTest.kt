package org.example.keycloak.schemas

import jakarta.ws.rs.core.UriBuilder
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.keycloak.models.KeycloakUriInfo
import org.keycloak.models.RealmModel
import org.keycloak.models.UserModel
import org.mockito.Mockito.mock
import org.mockito.Mockito.`when`

class ScimUserResponseTest {

    @Test
    @DisplayName("UserModelからScimUserResponseを生成できること")
    fun shouldCreateScimUserResponseFromUserModel() {
        val userModel = mock(UserModel::class.java).apply {
            `when`(id).thenReturn("0196EE58-EF67-C007-A708-00C1700184C2")
            `when`(username).thenReturn("taro.sato@example.org")
            `when`(isEnabled).thenReturn(true)
            `when`(firstName).thenReturn("Taro")
            `when`(lastName).thenReturn("Sato")
            `when`(email).thenReturn("taro.sato@example.org")
        }
        
        val realm = mock(RealmModel::class.java).apply {
            `when`(name).thenReturn("test-realm")
        }
        
        val uri = mock(KeycloakUriInfo::class.java).apply {
            `when`(baseUriBuilder).thenReturn(UriBuilder.fromUri("http://localhost:8080"))
        }
        
        // when
        val scimUserResponse = ScimUserResponse(userModel, realm, uri)
        
        // then
        assertThat(scimUserResponse)
            .extracting(
                { it.schemas },
                { it.id },
                { it.userName },
                { it.active },
                { it.name.familyName },
                { it.name.givenName },
                { it.emails.first().value },
                { it.emails.first().primary },
                { it.meta.resourceType },
                { it.meta.location }
            )
            .containsExactly(
                listOf("urn:ietf:params:scim:schemas:core:2.0:User"),
                "0196EE58-EF67-C007-A708-00C1700184C2",
                "taro.sato@example.org",
                true,
                "Sato",
                "Taro",
                "taro.sato@example.org",
                true,
                "User",
                "http://localhost:8080/realms/test-realm/scim/v2/Users/0196EE58-EF67-C007-A708-00C1700184C2"
            )
    }
}