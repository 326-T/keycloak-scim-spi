package org.example.keycloak.scim;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

public class ScimResourceProvider implements RealmResourceProvider {

  private final KeycloakSession session;

  public ScimResourceProvider(KeycloakSession session) {
    this.session = session;
  }

  @Override
  public Object getResource() {
    return this;
  }

  @Override
  public void close() {
  }

  @GET
  @Path("v2/Users")
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, String> hello() {
    return Map.of("message", "Hello, World!");
  }
}
