package org.example.keycloak.schemas;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import jakarta.ws.rs.core.UriBuilder;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.models.KeycloakUriInfo;
import org.keycloak.models.RealmModel;
import org.keycloak.models.UserModel;

class ScimUserResponseTest {

  @Test
  @DisplayName("UserModelからScimUserResponseを生成できること")
  void shouldCreateScimUserResponseFromUserModel() {
    var userModel = mock(UserModel.class);
    when(userModel.getId()).thenReturn("0196EE58-EF67-C007-A708-00C1700184C2");
    when(userModel.getUsername()).thenReturn("taro.sato@example.org");
    when(userModel.isEnabled()).thenReturn(true);
    when(userModel.getFirstName()).thenReturn("Taro");
    when(userModel.getLastName()).thenReturn("Sato");
    when(userModel.getEmail()).thenReturn("taro.sato@example.org");
    var realm = mock(RealmModel.class);
    when(realm.getName()).thenReturn("test-realm");
    var uri = mock(KeycloakUriInfo.class);
    when(uri.getBaseUriBuilder()).thenReturn(UriBuilder.fromUri("http://localhost:8080"));
    // when
    ScimUserResponse scimUserResponse = new ScimUserResponse(userModel, realm, uri);
    // then
    assertThat(scimUserResponse)
        .extracting(
            ScimUserResponse::schemas,
            ScimUserResponse::id,
            ScimUserResponse::userName,
            ScimUserResponse::active,
            u -> u.name().familyName(),
            u -> u.name().givenName(),
            u -> u.emails().getFirst().value(),
            u -> u.emails().getFirst().primary(),
            u -> u.meta().resourceType(),
            u -> u.meta().location()
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
            "User",
            "http://localhost:8080/realms/test-realm/scim/v2/Users/0196EE58-EF67-C007-A708-00C1700184C2"
        );
  }
}