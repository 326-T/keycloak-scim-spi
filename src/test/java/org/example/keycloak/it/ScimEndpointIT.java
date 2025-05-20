package org.example.keycloak.it;


import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import io.restassured.common.mapper.TypeRef;
import java.util.List;
import org.example.keycloak.schemas.ScimListResponse;
import org.example.keycloak.schemas.ScimUserResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
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
  class ScimListUsersEndpointIT {

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
              u -> u.emails().getFirst().primary()
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
                  true
              ),
              tuple(
                  List.of("urn:ietf:params:scim:schemas:core:2.0:User"),
                  "0196EE59-3C13-55F4-E475-DCD31F05F413",
                  "jiro.suzuki@example.org",
                  true,
                  "Suzuki",
                  "Jiro",
                  "jiro.suzuki@example.org",
                  true
              ),
              tuple(
                  List.of("urn:ietf:params:scim:schemas:core:2.0:User"),
                  "0196EE59-5858-5883-BAE4-9C539A3F13A6",
                  "saburo.tanaka@example.org",
                  true,
                  "Tanaka",
                  "Saburo",
                  "saburo.tanaka@example.org",
                  true
              )
          );
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
}
