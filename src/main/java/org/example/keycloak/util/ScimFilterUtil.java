package org.example.keycloak.util;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ScimFilterUtil {

  private ScimFilterUtil() {
  }

  /**
   * Parses SCIM filter string and extracts the userName.
   * Only supports the filter format: userName eq "value".
   *
   * @param encodedFilter the SCIM filter string
   *
   * @return a map containing the userName if found, otherwise an empty map
   */
  public static Map<String, String> parse(String encodedFilter) {
    if (encodedFilter == null || encodedFilter.isEmpty()) {
      return Map.of();
    }
    String filter = URLDecoder.decode(encodedFilter, StandardCharsets.UTF_8);
    Pattern p = Pattern.compile("^userName\\s+eq\\s+\"([^\"]*)\"$", Pattern.CASE_INSENSITIVE);
    Matcher m = p.matcher(filter);
    if (m.find()) {
      return Map.of(
          "username", m.group(1)
      );
    }
    return Map.of();
  }
}
