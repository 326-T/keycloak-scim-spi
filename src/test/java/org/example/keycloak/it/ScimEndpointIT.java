package org.example.keycloak.it;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import org.example.keycloak.schemas.ScimListResponse;
import org.example.keycloak.schemas.ScimUserResponse;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class ScimEndpointIT {

  @Container
  static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.2.4")
      .withRealmImportFile("/test-realm.json")
      .withProviderClassesFrom("target/classes");

  @Nested
  @DisplayName("SCIM API - List Users")
  class ListUsers {

    @Test
    @DisplayName("無条件検索")
    void shouldReturnListOfUsers() {
      // given
      String token = RestAssured.given()
          .contentType("application/x-www-form-urlencoded")
          .formParam("grant_type", "password")
          .formParam("client_id", "test-client")
          .formParam("username", "test-user")
          .formParam("password", "password")
          .post(keycloak.getAuthServerUrl() + "/realms/test/protocol/openid-connect/token")
          .then().extract().path("access_token");
      // when
      ScimListResponse<ScimUserResponse> body = RestAssured.given()
          .header("Authorization", "Bearer " + token)
          .get(keycloak.getAuthServerUrl() + "/realms/test/scim/v2/Users")
          .then()
          .statusCode(200)
          .extract().as(new TypeRef<>() {
          });
      // then
      assertThat(body.schemas()).isEqualTo(
          List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse"));
      assertThat(body.totalResults()).isEqualTo(3);
      assertThat(body.startIndex()).isZero();
      assertThat(body.itemsPerPage()).isEqualTo(100);
      assertThat(body.resources()).hasSize(3)
          .extracting(
              ScimUserResponse::schemas,
              ScimUserResponse::id,
              ScimUserResponse::userName,
              ScimUserResponse::active,
              u -> u.name().familyName(),
              u -> u.name().givenName(),
              u -> u.emails().getFirst().value(),
              u -> u.emails().getFirst().primary(),
              u -> u.meta().resourceType()
          )
          .containsExactlyInAnyOrder(
              tuple(
                  List.of("urn:ietf:params:scim:schemas:core:2.0:User"),
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
                  List.of("urn:ietf:params:scim:schemas:core:2.0:User"),
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
                  List.of("urn:ietf:params:scim:schemas:core:2.0:User"),
                  "0196EE59-5858-5883-BAE4-9C539A3F13A6",
                  "saburo.tanaka@example.org",
                  true,
                  "Tanaka",
                  "Saburo",
                  "saburo.tanaka@example.org",
                  true,
                  "User"
              )
          );
      assertThat(body.resources())
          .extracting(ScimUserResponse::meta)
          .extracting(ScimUserResponse.ScimUserMeta::location)
          .allMatch(location ->
              location.matches(
                  "^http://localhost:[0-9]+/realms/test/scim/v2/Users/[0-9a-fA-F-]+$"));
    }

    @Test
    @DisplayName("ページネーション")
    void shouldReturnListOfUsersWithPagination() {
      // given
      String token = RestAssured.given()
          .contentType("application/x-www-form-urlencoded")
          .formParam("grant_type", "password")
          .formParam("client_id", "test-client")
          .formParam("username", "test-user")
          .formParam("password", "password")
          .post(keycloak.getAuthServerUrl() + "/realms/test/protocol/openid-connect/token")
          .then().extract().path("access_token");
      // when
      ScimListResponse<ScimUserResponse> body = RestAssured.given()
          .header("Authorization", "Bearer " + token)
          .get(keycloak.getAuthServerUrl()
               + "/realms/test/scim/v2/Users?startIndex=2&itemsPerPage=3")
          .then()
          .statusCode(200)
          .extract().as(new TypeRef<>() {
          });
      // then
      assertThat(body.schemas()).isEqualTo(
          List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse"));
      assertThat(body.totalResults()).isEqualTo(3);
      assertThat(body.startIndex()).isEqualTo(2);
      assertThat(body.itemsPerPage()).isEqualTo(3);
      assertThat(body.resources()).hasSize(1);
    }

    @Test
    @DisplayName("userNameでフィルタリング")
    void shouldReturnListOfUsersWithFilter() {
      // given
      String token = RestAssured.given()
          .contentType("application/x-www-form-urlencoded")
          .formParam("grant_type", "password")
          .formParam("client_id", "test-client")
          .formParam("username", "test-user")
          .formParam("password", "password")
          .post(keycloak.getAuthServerUrl() + "/realms/test/protocol/openid-connect/token")
          .then().extract().path("access_token");
      // when
      ScimListResponse<ScimUserResponse> body = RestAssured.given()
          .header("Authorization", "Bearer " + token)
          .get(keycloak.getAuthServerUrl()
               + "/realms/test/scim/v2/Users?filter=userName+eq+%22taro.sato%40example.org%22")
          .then()
          .statusCode(200)
          .extract().as(new TypeRef<>() {
          });
      // then
      assertThat(body.schemas()).isEqualTo(
          List.of("urn:ietf:params:scim:api:messages:2.0:ListResponse"));
      assertThat(body.totalResults()).isEqualTo(1);
      assertThat(body.startIndex()).isZero();
      assertThat(body.itemsPerPage()).isEqualTo(100);
      assertThat(body.resources()).hasSize(1)
          .extracting(
              ScimUserResponse::id,
              ScimUserResponse::userName
          )
          .containsExactly(
              tuple(
                  "0196EE58-EF67-C007-A708-00C1700184C2",
                  "taro.sato@example.org"
              )
          );
    }
  }

  @Nested
  @DisplayName("SCIM API - Get User")
  class GetUser {

    @Test
    @DisplayName("ユーザー情報取得")
    void shouldReturnUserInfo() {
      // given
      String token = RestAssured.given()
          .contentType("application/x-www-form-urlencoded")
          .formParam("grant_type", "password")
          .formParam("client_id", "test-client")
          .formParam("username", "test-user")
          .formParam("password", "password")
          .post(keycloak.getAuthServerUrl() + "/realms/test/protocol/openid-connect/token")
          .then().extract().path("access_token");
      // when
      ScimUserResponse body = RestAssured.given()
          .header("Authorization", "Bearer " + token)
          .get(keycloak.getAuthServerUrl()
               + "/realms/test/scim/v2/Users/0196EE58-EF67-C007-A708-00C1700184C2")
          .then()
          .statusCode(200)
          .extract().as(new TypeRef<>() {
          });
      // then
      assertThat(body)
          .extracting(
              ScimUserResponse::schemas,
              ScimUserResponse::id,
              ScimUserResponse::userName,
              ScimUserResponse::active,
              u -> u.name().familyName(),
              u -> u.name().givenName(),
              u -> u.emails().getFirst().value(),
              u -> u.emails().getFirst().primary(),
              u -> u.meta().resourceType()
          )
          .containsExactly(
              List.of("urn:ietf:params:scim:schemas:core:2.0:User"),
              "0196EE58-EF67-C007-A708-00C1700184C2",
              "taro.sato@example.org",
              true,
              "Sato",
              "Taro",
              "taro.sato@example.org",
              true,
              "User"
          );
      assertThat(body.meta().location()).matches(
          "^http://localhost:[0-9]+/realms/test/scim/v2/Users/0196EE58-EF67-C007-A708-00C1700184C2$");
    }
  }

  @Nested
  @DisplayName("SCIM API - Create User")
  class createUser {

    @AfterAll
    static void cleanup() {
      keycloak.stop();
      keycloak.start();
    }

    @Test
    @DisplayName("新規ユーザー作成")
    void shouldCreateUser() {
      // given
      String token = RestAssured.given()
          .contentType("application/x-www-form-urlencoded")
          .formParam("grant_type", "password")
          .formParam("client_id", "test-client")
          .formParam("username", "test-user")
          .formParam("password", "password")
          .post(keycloak.getAuthServerUrl() + "/realms/test/protocol/openid-connect/token")
          .then().extract().path("access_token");
      // when
      ScimUserResponse body = RestAssured.given()
          .header("Authorization", "Bearer " + token)
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
              """)
          .when()
          .post(keycloak.getAuthServerUrl()
                + "/realms/test/scim/v2/Users")

          .then()
          .statusCode(201)
          .extract().as(new TypeRef<>() {
          });
      // then
      assertThat(body.id()).isNotNull();
      assertThat(body)
          .extracting(
              ScimUserResponse::schemas,
              ScimUserResponse::userName,
              ScimUserResponse::active,
              u -> u.name().familyName(),
              u -> u.name().givenName(),
              u -> u.emails().getFirst().value(),
              u -> u.emails().getFirst().primary(),
              u -> u.meta().resourceType()
          )
          .containsExactly(
              List.of("urn:ietf:params:scim:schemas:core:2.0:User"),
              "shiro.saito@example.org",
              true,
              "Saito",
              "Shiro",
              "shiro.saito@example.org",
              true,
              "User"
          );
      assertThat(body.meta().location()).matches(
          "^http://localhost:[0-9]+/realms/test/scim/v2/Users/[0-9a-fA-F-]+$");
      ScimUserResponse created = RestAssured.given()
          .header("Authorization", "Bearer " + token)
          .get(body.meta().location())
          .then()
          .statusCode(200)
          .extract().as(new TypeRef<>() {
          });
      assertThat(created)
          .extracting(
              ScimUserResponse::schemas,
              ScimUserResponse::userName,
              ScimUserResponse::active,
              u -> u.name().familyName(),
              u -> u.name().givenName(),
              u -> u.emails().getFirst().value(),
              u -> u.emails().getFirst().primary(),
              u -> u.meta().resourceType()
          )
          .containsExactly(
              List.of("urn:ietf:params:scim:schemas:core:2.0:User"),
              "shiro.saito@example.org",
              true,
              "Saito",
              "Shiro",
              "shiro.saito@example.org",
              true,
              "User"
          );
    }
  }

  @Nested
  @DisplayName("SCIM API - Patch User")
  class patchUser {

    @AfterAll
    static void cleanup() {
      keycloak.stop();
      keycloak.start();
    }

    @ParameterizedTest
    @ValueSource(strings = {
        "Replace", "Add"
    })
    @DisplayName("ユーザー情報更新")
    void shouldUpdateUser(String operation) {
      // given
      String token = RestAssured.given()
          .contentType("application/x-www-form-urlencoded")
          .formParam("grant_type", "password")
          .formParam("client_id", "test-client")
          .formParam("username", "test-user")
          .formParam("password", "password")
          .post(keycloak.getAuthServerUrl() + "/realms/test/protocol/openid-connect/token")
          .then().extract().path("access_token");
      // when
      ScimUserResponse body = RestAssured.given()
          .header("Authorization", "Bearer " + token)
          .contentType("application/json")
          .body("""
              {
                "schemas": [
                  "urn:ietf:params:scim:api:messages:2.0:PatchOp"
                ],
                "Operations": [
                  {
                    "op": "%s",
                    "path": "emails[type eq \\"work\\"].value",
                    "value": "taro.sato@example.com"
                  },
                  {
                    "op": "%s",
                    "path": "name.givenName",
                    "value": "taro"
                  },
                  {
                    "op": "%s",
                    "path": "name.familyName",
                    "value": "sato"
                  },
                  {
                    "op": "%s",
                    "path": "active",
                    "value": "False"
                  }
                ]
              }
              """.formatted(operation, operation, operation, operation))
          .when()
          .patch(keycloak.getAuthServerUrl()
                 + "/realms/test/scim/v2/Users/0196EE58-EF67-C007-A708-00C1700184C2")

          .then()
          .statusCode(200)
          .extract().as(new TypeRef<>() {
          });
      // then
      assertThat(body)
          .extracting(
              ScimUserResponse::schemas,
              ScimUserResponse::userName,
              ScimUserResponse::active,
              u -> u.name().familyName(),
              u -> u.name().givenName(),
              u -> u.emails().getFirst().value(),
              u -> u.emails().getFirst().primary(),
              u -> u.meta().resourceType()
          )
          .containsExactly(
              List.of("urn:ietf:params:scim:schemas:core:2.0:User"),
              "taro.sato@example.org",
              false,
              "sato",
              "taro",
              "taro.sato@example.com",
              true,
              "User"
          );
      assertThat(body.meta().location()).matches(
          "^http://localhost:[0-9]+/realms/test/scim/v2/Users/0196EE58-EF67-C007-A708-00C1700184C2$");
      ScimUserResponse updated = RestAssured.given()
          .header("Authorization", "Bearer " + token)
          .get(body.meta().location())
          .then()
          .statusCode(200)
          .extract().as(new TypeRef<>() {
          });
      assertThat(updated)
          .extracting(
              ScimUserResponse::schemas,
              ScimUserResponse::userName,
              ScimUserResponse::active,
              u -> u.name().familyName(),
              u -> u.name().givenName(),
              u -> u.emails().getFirst().value(),
              u -> u.emails().getFirst().primary(),
              u -> u.meta().resourceType()
          )
          .containsExactly(
              List.of("urn:ietf:params:scim:schemas:core:2.0:User"),
              "taro.sato@example.org",
              false,
              "sato",
              "taro",
              "taro.sato@example.com",
              true,
              "User"
          );
    }

    @Test
    @DisplayName("ユーザー情報更新 - Remove")
    void shouldUpdateUserWithRemove() {
      // given
      String token = RestAssured.given()
          .contentType("application/x-www-form-urlencoded")
          .formParam("grant_type", "password")
          .formParam("client_id", "test-client")
          .formParam("username", "test-user")
          .formParam("password", "password")
          .post(keycloak.getAuthServerUrl() + "/realms/test/protocol/openid-connect/token")
          .then().extract().path("access_token");
      // when
      ScimUserResponse body = RestAssured.given()
          .header("Authorization", "Bearer " + token)
          .contentType("application/json")
          .body("""
              {
                "schemas": [
                  "urn:ietf:params:scim:api:messages:2.0:PatchOp"
                ],
                "Operations": [
                  {
                    "op": "Remove",
                    "path": "emails[type eq \\"work\\"].value",
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
              """)
          .when()
          .patch(keycloak.getAuthServerUrl()
                 + "/realms/test/scim/v2/Users/0196EE58-EF67-C007-A708-00C1700184C2")

          .then()
          .statusCode(200)
          .extract().as(new TypeRef<>() {
          });
      // then
      assertThat(body)
          .extracting(
              ScimUserResponse::schemas,
              ScimUserResponse::userName,
              ScimUserResponse::active,
              u -> u.name().familyName(),
              u -> u.name().givenName(),
              u -> u.emails().getFirst().value(),
              u -> u.emails().getFirst().primary(),
              u -> u.meta().resourceType()
          )
          .containsExactly(
              List.of("urn:ietf:params:scim:schemas:core:2.0:User"),
              "taro.sato@example.org",
              false,
              null,
              null,
              null,
              true,
              "User"
          );
      assertThat(body.meta().location()).matches(
          "^http://localhost:[0-9]+/realms/test/scim/v2/Users/0196EE58-EF67-C007-A708-00C1700184C2$");
      ScimUserResponse updated = RestAssured.given()
          .header("Authorization", "Bearer " + token)
          .get(body.meta().location())
          .then()
          .statusCode(200)
          .extract().as(new TypeRef<>() {
          });
      assertThat(updated)
          .extracting(
              ScimUserResponse::schemas,
              ScimUserResponse::userName,
              ScimUserResponse::active,
              u -> u.name().familyName(),
              u -> u.name().givenName(),
              u -> u.emails().getFirst().value(),
              u -> u.emails().getFirst().primary(),
              u -> u.meta().resourceType()
          )
          .containsExactly(
              List.of("urn:ietf:params:scim:schemas:core:2.0:User"),
              "taro.sato@example.org",
              false,
              null,
              null,
              null,
              true,
              "User"
          );
    }
  }
}
