package org.example.keycloak.it;

import dasniko.testcontainers.keycloak.KeycloakContainer;
import io.restassured.RestAssured;
import org.junit.jupiter.api.Test;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
class HelloEndpointIT {

  @Container
  static KeycloakContainer keycloak = new KeycloakContainer("quay.io/keycloak/keycloak:26.2.4")
      .withRealmImportFile("/test-realm.json")
      .withProviderClassesFrom("target/classes");

  @Test
  void helloEndpoint_shouldReturnHelloWorld() {
    // トークン取得
    String token = RestAssured.given()
        .contentType("application/x-www-form-urlencoded")
        .formParam("grant_type", "password")
        .formParam("client_id", "test-client")
        .formParam("username", "test-user")
        .formParam("password", "password")
        .post(keycloak.getAuthServerUrl() + "/realms/test/protocol/openid-connect/token")
        .then().extract().path("access_token");

    // /hello 呼び出し検証
    RestAssured.given()
        .header("Authorization", "Bearer " + token)
        .get(keycloak.getAuthServerUrl() + "/realms/test/hello")
        .then()
        .statusCode(200)
        .body("message", org.hamcrest.Matchers.equalTo("Hello, World!"));
  }
}
