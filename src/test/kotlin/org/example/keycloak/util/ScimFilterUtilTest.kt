package org.example.keycloak.util

import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test

class ScimFilterUtilTest {

    @Nested
    inner class Parse {

        @Test
        fun shouldParseFilter() {
            // given
            val filter = "userName+eq+%22taro.sato%40example.org%22"
            // when
            val parsed = ScimFilterUtil.parse(filter)
            // then
            assertThat(parsed).isEqualTo(mapOf("username" to "taro.sato@example.org"))
        }
    }
}