package org.example.keycloak.hello;

import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;
import java.util.Map;
import org.keycloak.models.KeycloakSession;
import org.keycloak.services.resource.RealmResourceProvider;

@Path("hello")
public class HelloResourceProvider implements RealmResourceProvider {

  private final KeycloakSession session;

  public HelloResourceProvider(KeycloakSession session) {
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
  @Produces(MediaType.APPLICATION_JSON)
  public Map<String, String> hello() {
    return Map.of("message", "Hello, World!");
  }
}
