package org.example.keycloak.util

import java.net.URLDecoder
import java.nio.charset.StandardCharsets
import java.util.regex.Pattern

object ScimFilterUtil {
    /**
     * Parses SCIM filter string and extracts the userName.
     * Only supports the filter format: userName eq "value".
     *
     * @param encodedFilter the SCIM filter string
     * @return a map containing the userName if found, otherwise an empty map
     */
    fun parse(encodedFilter: String?): Map<String, String> {
        if (encodedFilter.isNullOrEmpty()) {
            return emptyMap()
        }
        
        val filter = URLDecoder.decode(encodedFilter, StandardCharsets.UTF_8)
        val pattern = Pattern.compile("^userName\\s+eq\\s+\"([^\"]*)\"$", Pattern.CASE_INSENSITIVE)
        val matcher = pattern.matcher(filter)
        
        return if (matcher.find()) {
            mapOf("username" to matcher.group(1))
        } else {
            emptyMap()
        }
    }
}