package org.example.keycloak.schemas;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.keycloak.models.ClientModel;
import org.keycloak.models.GroupModel;
import org.keycloak.models.RoleModel;
import org.keycloak.models.SubjectCredentialManager;
import org.keycloak.models.UserModel;

class ScimUserResponseTest {

  @Test
  @DisplayName("UserModelからScimUserResponseを生成できること")
  void shouldCreateScimUserResponseFromUserModel() {
    UserModel userModel = new UserModel() {
      @Override
      public String getId() {
        return "0196EE58-EF67-C007-A708-00C1700184C2";
      }

      @Override
      public String getUsername() {
        return "taro.sato@example.org";
      }

      @Override
      public void setUsername(String s) {

      }

      @Override
      public Long getCreatedTimestamp() {
        return null;
      }

      @Override
      public void setCreatedTimestamp(Long aLong) {

      }

      @Override
      public boolean isEnabled() {
        return true;
      }

      @Override
      public void setEnabled(boolean b) {

      }

      @Override
      public void setSingleAttribute(String s, String s1) {

      }

      @Override
      public void setAttribute(String s, List<String> list) {

      }

      @Override
      public void removeAttribute(String s) {

      }

      @Override
      public String getFirstAttribute(String s) {
        return null;
      }

      @Override
      public Stream<String> getAttributeStream(String s) {
        return null;
      }

      @Override
      public Map<String, List<String>> getAttributes() {
        return null;
      }

      @Override
      public Stream<String> getRequiredActionsStream() {
        return null;
      }

      @Override
      public void addRequiredAction(String s) {

      }

      @Override
      public void removeRequiredAction(String s) {

      }

      @Override
      public String getFirstName() {
        return "Taro";
      }

      @Override
      public void setFirstName(String s) {

      }

      @Override
      public String getLastName() {
        return "Sato";
      }

      @Override
      public void setLastName(String s) {

      }

      @Override
      public String getEmail() {
        return "taro.sato@example.org";
      }

      @Override
      public void setEmail(String s) {

      }

      @Override
      public boolean isEmailVerified() {
        return false;
      }

      @Override
      public void setEmailVerified(boolean b) {

      }

      @Override
      public Stream<GroupModel> getGroupsStream() {
        return null;
      }

      @Override
      public void joinGroup(GroupModel groupModel) {

      }

      @Override
      public void leaveGroup(GroupModel groupModel) {

      }

      @Override
      public boolean isMemberOf(GroupModel groupModel) {
        return false;
      }

      @Override
      public String getFederationLink() {
        return null;
      }

      @Override
      public void setFederationLink(String s) {

      }

      @Override
      public String getServiceAccountClientLink() {
        return null;
      }

      @Override
      public void setServiceAccountClientLink(String s) {

      }

      @Override
      public SubjectCredentialManager credentialManager() {
        return null;
      }

      @Override
      public Stream<RoleModel> getRealmRoleMappingsStream() {
        return null;
      }

      @Override
      public Stream<RoleModel> getClientRoleMappingsStream(ClientModel clientModel) {
        return null;
      }

      @Override
      public boolean hasRole(RoleModel roleModel) {
        return false;
      }

      @Override
      public void grantRole(RoleModel roleModel) {

      }

      @Override
      public Stream<RoleModel> getRoleMappingsStream() {
        return null;
      }

      @Override
      public void deleteRoleMapping(RoleModel roleModel) {

      }
    };
    // when
    ScimUserResponse scimUserResponse = new ScimUserResponse(userModel);
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
            u -> u.emails().getFirst().primary()
        )
        .containsExactly(
            List.of("urn:ietf:params:scim:schemas:core:2.0:User"),
            "0196EE58-EF67-C007-A708-00C1700184C2",
            "taro.sato@example.org",
            true,
            "Sato",
            "Taro",
            "taro.sato@example.org",
            true
        );
  }
}