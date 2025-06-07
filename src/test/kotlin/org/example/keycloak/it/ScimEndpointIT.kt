package org.example.keycloak.it

import dasniko.testcontainers.keycloak.KeycloakContainer
import io.restassured.RestAssured
import io.restassured.common.mapper.TypeRef
import org.assertj.core.api.Assertions.assertThat
import org.assertj.core.api.Assertions.tuple
import org.example.keycloak.schemas.ScimListResponse
import org.example.keycloak.schemas.ScimUserResponse
import org.junit.jupiter.api.AfterAll
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.params.ParameterizedTest
import org.junit.jupiter.params.provider.ValueSource
import org.testcontainers.junit.jupiter.Container
import org.testcontainers.junit.jupiter.Testcontainers

@Testcontainers
class ScimEndpointIT {

    companion object {
        @Container
        @JvmStatic
        val keycloak = KeycloakContainer("quay.io/keycloak/keycloak:26.2.4")
            .withRealmImportFile("/test-realm.json")
            .withProviderClassesFrom("target/classes")
    }

    @Nested
    @DisplayName("SCIM API - List Users")
    inner class ListUsers {

        @Test
        @DisplayName("無条件検索")
        fun shouldReturnListOfUsers() {
            // given
            val token = RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", "test-client")
                .formParam("username", "test-user")
                .formParam("password", "password")
                .post("${keycloak.authServerUrl}/realms/test/protocol/openid-connect/token")
                .then().extract().path<String>("access_token")

            // when
            val body = RestAssured.given()
                .header("Authorization", "Bearer $token")
                .get("${keycloak.authServerUrl}/realms/test/scim/v2/Users")
                .then()
                .statusCode(200)
                .extract().`as`(object : TypeRef<ScimListResponse<ScimUserResponse>>() {})

            // then
            assertThat(body.schemas).isEqualTo(
                listOf("urn:ietf:params:scim:api:messages:2.0:ListResponse")
            )
            assertThat(body.totalResults).isEqualTo(3)
            assertThat(body.startIndex).isZero()
            assertThat(body.itemsPerPage).isEqualTo(100)
            assertThat(body.resources).hasSize(3)
                .extracting(
                    { it.schemas },
                    { it.id },
                    { it.userName },
                    { it.active },
                    { it.name.familyName },
                    { it.name.givenName },
                    { it.emails.first().value },
                    { it.emails.first().primary },
                    { it.meta.resourceType }
                )
                .containsExactlyInAnyOrder(
                    tuple(
                        listOf("urn:ietf:params:scim:schemas:core:2.0:User"),
                        "0196EE58-EF67-C007-A708-00C1700184C2",
                        "taro.sato@example.org",
                        true,
                        "Sato",
                        "Taro",
                        "taro.sato@example.org",
                        true,
                        "User"
                    ),
                    tuple(
                        listOf("urn:ietf:params:scim:schemas:core:2.0:User"),
                        "0196EE59-3C13-55F4-E475-DCD31F05F413",
                        "jiro.suzuki@example.org",
                        true,
                        "Suzuki",
                        "Jiro",
                        "jiro.suzuki@example.org",
                        true,
                        "User"
                    ),
                    tuple(
                        listOf("urn:ietf:params:scim:schemas:core:2.0:User"),
                        "0196EE59-5858-5883-BAE4-9C539A3F13A6",
                        "saburo.tanaka@example.org",
                        true,
                        "Tanaka",
                        "Saburo",
                        "saburo.tanaka@example.org",
                        true,
                        "User"
                    )
                )
            assertThat(body.resources)
                .extracting<ScimUserResponse.ScimUserMeta> { it.meta }
                .extracting<String> { it.location }
                .allMatch { location ->
                    location.matches("^http://localhost:[0-9]+/realms/test/scim/v2/Users/[0-9a-fA-F-]+$".toRegex())
                }
        }

        @Test
        @DisplayName("ページネーション")
        fun shouldReturnListOfUsersWithPagination() {
            // given
            val token = RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", "test-client")
                .formParam("username", "test-user")
                .formParam("password", "password")
                .post("${keycloak.authServerUrl}/realms/test/protocol/openid-connect/token")
                .then().extract().path<String>("access_token")

            // when
            val body = RestAssured.given()
                .header("Authorization", "Bearer $token")
                .get("${keycloak.authServerUrl}/realms/test/scim/v2/Users?startIndex=2&itemsPerPage=3")
                .then()
                .statusCode(200)
                .extract().`as`(object : TypeRef<ScimListResponse<ScimUserResponse>>() {})

            // then
            assertThat(body.schemas).isEqualTo(
                listOf("urn:ietf:params:scim:api:messages:2.0:ListResponse")
            )
            assertThat(body.totalResults).isEqualTo(3)
            assertThat(body.startIndex).isEqualTo(2)
            assertThat(body.itemsPerPage).isEqualTo(3)
            assertThat(body.resources).hasSize(1)
        }

        @Test
        @DisplayName("userNameでフィルタリング")
        fun shouldReturnListOfUsersWithFilter() {
            // given
            val token = RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", "test-client")
                .formParam("username", "test-user")
                .formParam("password", "password")
                .post("${keycloak.authServerUrl}/realms/test/protocol/openid-connect/token")
                .then().extract().path<String>("access_token")

            // when
            val body = RestAssured.given()
                .header("Authorization", "Bearer $token")
                .get("${keycloak.authServerUrl}/realms/test/scim/v2/Users?filter=userName+eq+%22taro.sato%40example.org%22")
                .then()
                .statusCode(200)
                .extract().`as`(object : TypeRef<ScimListResponse<ScimUserResponse>>() {})

            // then
            assertThat(body.schemas).isEqualTo(
                listOf("urn:ietf:params:scim:api:messages:2.0:ListResponse")
            )
            assertThat(body.totalResults).isEqualTo(1)
            assertThat(body.startIndex).isZero()
            assertThat(body.itemsPerPage).isEqualTo(100)
            assertThat(body.resources).hasSize(1)
                .extracting(
                    { it.id },
                    { it.userName }
                )
                .containsExactly(
                    tuple(
                        "0196EE58-EF67-C007-A708-00C1700184C2",
                        "taro.sato@example.org"
                    )
                )
        }
    }

    @Nested
    @DisplayName("SCIM API - Get User")
    inner class GetUser {

        @Test
        @DisplayName("ユーザー情報取得")
        fun shouldReturnUserInfo() {
            // given
            val token = RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", "test-client")
                .formParam("username", "test-user")
                .formParam("password", "password")
                .post("${keycloak.authServerUrl}/realms/test/protocol/openid-connect/token")
                .then().extract().path<String>("access_token")

            // when
            val body = RestAssured.given()
                .header("Authorization", "Bearer $token")
                .get("${keycloak.authServerUrl}/realms/test/scim/v2/Users/0196EE58-EF67-C007-A708-00C1700184C2")
                .then()
                .statusCode(200)
                .extract().`as`(object : TypeRef<ScimUserResponse>() {})

            // then
            assertThat(body)
                .extracting(
                    { it.schemas },
                    { it.id },
                    { it.userName },
                    { it.active },
                    { it.name.familyName },
                    { it.name.givenName },
                    { it.emails.first().value },
                    { it.emails.first().primary },
                    { it.meta.resourceType }
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
                    "User"
                )
            assertThat(body.meta.location).matches(
                "^http://localhost:[0-9]+/realms/test/scim/v2/Users/0196EE58-EF67-C007-A708-00C1700184C2$"
            )
        }
    }

    @Nested
    @DisplayName("SCIM API - Create User")
    inner class CreateUser {

        @AfterAll
        fun cleanup() {
            keycloak.stop()
            keycloak.start()
        }

        @Test
        @DisplayName("新規ユーザー作成")
        fun shouldCreateUser() {
            // given
            val token = RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", "test-client")
                .formParam("username", "test-user")
                .formParam("password", "password")
                .post("${keycloak.authServerUrl}/realms/test/protocol/openid-connect/token")
                .then().extract().path<String>("access_token")

            // when
            val body = RestAssured.given()
                .header("Authorization", "Bearer $token")
                .contentType("application/json")
                .body("""
                    {
                      "schemas": [
                        "urn:ietf:params:scim:schemas:core:2.0:User",
                        "urn:ietf:params:scim:schemas:extension:enterprise:2.0:User"
                      ],
                      "externalId": "shiro.saito",
                      "userName": "shiro.saito@example.org",
                      "active": true,
                      "displayName": "Shiro Saito",
                      "emails": [
                        { "primary": true, "type": "work", "value": "shiro.saito@example.org" }
                      ],
                      "meta": { "resourceType": "User" },
                      "name": {
                        "formatted": "Shiro Saito",
                        "familyName": "Saito",
                        "givenName": "Shiro"
                      }
                    }
                    """.trimIndent())
                .`when`()
                .post("${keycloak.authServerUrl}/realms/test/scim/v2/Users")
                .then()
                .statusCode(201)
                .extract().`as`(object : TypeRef<ScimUserResponse>() {})

            // then
            assertThat(body.id).isNotNull()
            assertThat(body)
                .extracting(
                    { it.schemas },
                    { it.userName },
                    { it.active },
                    { it.name.familyName },
                    { it.name.givenName },
                    { it.emails.first().value },
                    { it.emails.first().primary },
                    { it.meta.resourceType }
                )
                .containsExactly(
                    listOf("urn:ietf:params:scim:schemas:core:2.0:User"),
                    "shiro.saito@example.org",
                    true,
                    "Saito",
                    "Shiro",
                    "shiro.saito@example.org",
                    true,
                    "User"
                )
            assertThat(body.meta.location).matches(
                "^http://localhost:[0-9]+/realms/test/scim/v2/Users/[0-9a-fA-F-]+$"
            )
            
            val created = RestAssured.given()
                .header("Authorization", "Bearer $token")
                .get(body.meta.location)
                .then()
                .statusCode(200)
                .extract().`as`(object : TypeRef<ScimUserResponse>() {})
            
            assertThat(created)
                .extracting(
                    { it.schemas },
                    { it.userName },
                    { it.active },
                    { it.name.familyName },
                    { it.name.givenName },
                    { it.emails.first().value },
                    { it.emails.first().primary },
                    { it.meta.resourceType }
                )
                .containsExactly(
                    listOf("urn:ietf:params:scim:schemas:core:2.0:User"),
                    "shiro.saito@example.org",
                    true,
                    "Saito",
                    "Shiro",
                    "shiro.saito@example.org",
                    true,
                    "User"
                )
        }
    }

    @Nested
    @DisplayName("SCIM API - Patch User")
    inner class PatchUser {

        @AfterAll
        fun cleanup() {
            keycloak.stop()
            keycloak.start()
        }

        @ParameterizedTest
        @ValueSource(strings = ["Replace", "Add"])
        @DisplayName("ユーザー情報更新")
        fun shouldUpdateUser(operation: String) {
            // given
            val token = RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", "test-client")
                .formParam("username", "test-user")
                .formParam("password", "password")
                .post("${keycloak.authServerUrl}/realms/test/protocol/openid-connect/token")
                .then().extract().path<String>("access_token")

            // when
            val body = RestAssured.given()
                .header("Authorization", "Bearer $token")
                .contentType("application/json")
                .body("""
                    {
                      "schemas": [
                        "urn:ietf:params:scim:api:messages:2.0:PatchOp"
                      ],
                      "Operations": [
                        {
                          "op": "$operation",
                          "path": "emails[type eq \"work\"].value",
                          "value": "taro.sato@example.com"
                        },
                        {
                          "op": "$operation",
                          "path": "name.givenName",
                          "value": "taro"
                        },
                        {
                          "op": "$operation",
                          "path": "name.familyName",
                          "value": "sato"
                        },
                        {
                          "op": "$operation",
                          "path": "active",
                          "value": "False"
                        }
                      ]
                    }
                    """.trimIndent())
                .`when`()
                .patch("${keycloak.authServerUrl}/realms/test/scim/v2/Users/0196EE58-EF67-C007-A708-00C1700184C2")
                .then()
                .statusCode(200)
                .extract().`as`(object : TypeRef<ScimUserResponse>() {})

            // then
            assertThat(body)
                .extracting(
                    { it.schemas },
                    { it.userName },
                    { it.active },
                    { it.name.familyName },
                    { it.name.givenName },
                    { it.emails.first().value },
                    { it.emails.first().primary },
                    { it.meta.resourceType }
                )
                .containsExactly(
                    listOf("urn:ietf:params:scim:schemas:core:2.0:User"),
                    "taro.sato@example.org",
                    false,
                    "sato",
                    "taro",
                    "taro.sato@example.com",
                    true,
                    "User"
                )
            assertThat(body.meta.location).matches(
                "^http://localhost:[0-9]+/realms/test/scim/v2/Users/0196EE58-EF67-C007-A708-00C1700184C2$"
            )
            
            val updated = RestAssured.given()
                .header("Authorization", "Bearer $token")
                .get(body.meta.location)
                .then()
                .statusCode(200)
                .extract().`as`(object : TypeRef<ScimUserResponse>() {})
            
            assertThat(updated)
                .extracting(
                    { it.schemas },
                    { it.userName },
                    { it.active },
                    { it.name.familyName },
                    { it.name.givenName },
                    { it.emails.first().value },
                    { it.emails.first().primary },
                    { it.meta.resourceType }
                )
                .containsExactly(
                    listOf("urn:ietf:params:scim:schemas:core:2.0:User"),
                    "taro.sato@example.org",
                    false,
                    "sato",
                    "taro",
                    "taro.sato@example.com",
                    true,
                    "User"
                )
        }

        @Test
        @DisplayName("ユーザー情報更新 - Remove")
        fun shouldUpdateUserWithRemove() {
            // given
            val token = RestAssured.given()
                .contentType("application/x-www-form-urlencoded")
                .formParam("grant_type", "password")
                .formParam("client_id", "test-client")
                .formParam("username", "test-user")
                .formParam("password", "password")
                .post("${keycloak.authServerUrl}/realms/test/protocol/openid-connect/token")
                .then().extract().path<String>("access_token")

            // when
            val body = RestAssured.given()
                .header("Authorization", "Bearer $token")
                .contentType("application/json")
                .body("""
                    {
                      "schemas": [
                        "urn:ietf:params:scim:api:messages:2.0:PatchOp"
                      ],
                      "Operations": [
                        {
                          "op": "Remove",
                          "path": "emails[type eq \"work\"].value",
                          "value": "taro.sato@example.com"
                        },
                        {
                          "op": "Remove",
                          "path": "name.givenName",
                          "value": "taro"
                        },
                        {
                          "op": "Remove",
                          "path": "name.familyName",
                          "value": "sato"
                        },
                        {
                          "op": "Remove",
                          "path": "active",
                          "value": "False"
                        }
                      ]
                    }
                    """.trimIndent())
                .`when`()
                .patch("${keycloak.authServerUrl}/realms/test/scim/v2/Users/0196EE58-EF67-C007-A708-00C1700184C2")
                .then()
                .statusCode(200)
                .extract().`as`(object : TypeRef<ScimUserResponse>() {})

            // then
            assertThat(body)
                .extracting(
                    { it.schemas },
                    { it.userName },
                    { it.active },
                    { it.name.familyName },
                    { it.name.givenName },
                    { it.emails.first().value },
                    { it.emails.first().primary },
                    { it.meta.resourceType }
                )
                .containsExactly(
                    listOf("urn:ietf:params:scim:schemas:core:2.0:User"),
                    "taro.sato@example.org",
                    false,
                    null,
                    null,
                    null,
                    true,
                    "User"
                )
            assertThat(body.meta.location).matches(
                "^http://localhost:[0-9]+/realms/test/scim/v2/Users/0196EE58-EF67-C007-A708-00C1700184C2$"
            )
            
            val updated = RestAssured.given()
                .header("Authorization", "Bearer $token")
                .get(body.meta.location)
                .then()
                .statusCode(200)
                .extract().`as`(object : TypeRef<ScimUserResponse>() {})
            
            assertThat(updated)
                .extracting(
                    { it.schemas },
                    { it.userName },
                    { it.active },
                    { it.name.familyName },
                    { it.name.givenName },
                    { it.emails.first().value },
                    { it.emails.first().primary },
                    { it.meta.resourceType }
                )
                .containsExactly(
                    listOf("urn:ietf:params:scim:schemas:core:2.0:User"),
                    "taro.sato@example.org",
                    false,
                    null,
                    null,
                    null,
                    true,
                    "User"
                )
        }
    }
}