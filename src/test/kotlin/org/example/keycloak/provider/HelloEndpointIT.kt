package org.example.keycloak.provider

import dasniko.testcontainers.keycloak.KeycloakContainer
import io.restassured.RestAssured
import org.hamcrest.Matchers
import org.junit.jupiter.api.Test
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers
import java.io.File

@Testcontainers
class HelloEndpointIT {

    companion object {
        @Container
        val keycloak = KeycloakContainer("quay.io/keycloak/keycloak:26.2.4")
            .withRealmImportFile("/test-realm.json")
            .withProviderLibsFrom(
                listOf(
                    File("target/keycloak-scim-spi-1.0-SNAPSHOT.jar")
                )
            )
    }

    @Test
    fun helloEndpoint_shouldReturnHelloWorld() {
        // トークン取得
        val token = RestAssured.given()
            .contentType("application/x-www-form-urlencoded")
            .formParam("grant_type", "password")
            .formParam("client_id", "test-client")
            .formParam("username", "test-user")
            .formParam("password", "password")
            .post("${keycloak.authServerUrl}/realms/test/protocol/openid-connect/token")
            .then().extract().path<String>("access_token")

        // /hello 呼び出し検証
        RestAssured.given()
            .header("Authorization", "Bearer $token")
            .get("${keycloak.authServerUrl}/realms/test/hello")
            .then()
            .statusCode(200)
            .body("message", Matchers.equalTo("Hello, World!"))
    }
}