package org.example.keycloak.util;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

class ScimFilterUtilTest {

  @Nested
  class Parse {

    @Test
    void shouldParseFilter() {
      // given
      String filter = "userName+eq+%22taro.sato%40example.org%22";
      // when
      var parsed = ScimFilterUtil.parse(filter);
      // then
      assertThat(parsed).isEqualTo(Map.of("username", "taro.sato@example.org"));
    }
  }

}