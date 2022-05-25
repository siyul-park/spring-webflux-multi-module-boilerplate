package io.github.siyual_park.data.regexp

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

class SqlLikeTranspilerTest {
    @Test
    fun toRegEx() {
        val result = SqlLikeTranspiler.toRegEx("%abc[%]%")
        assertEquals("^.*\\Qabc\\E\\Q%\\E.*\$", result)

        val pattern = Pattern.compile(result)
        val matcher = pattern.matcher("affaabc%adfafa")
        assertTrue(matcher.find())
    }
}
