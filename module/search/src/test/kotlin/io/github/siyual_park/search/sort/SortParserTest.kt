package io.github.siyual_park.search.sort

import com.fasterxml.jackson.databind.ObjectMapper
import io.github.siyual_park.search.exception.SortInvalidException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import org.springframework.data.domain.Sort.Order

class SortParserTest {
    internal data class Person(
        var name: String,
        var age: Int
    )

    private val objectMapper = ObjectMapper()
    private val sortParser = SortParser(Person::class, objectMapper)

    @Test
    fun parse() {
        val sort = sortParser.parse("desc:name")
        assertEquals(Order.desc("name"), sort.getOrderFor("name"))

        assertThrows(SortInvalidException::class.java) { sortParser.parse("desc:not_found") }
        assertThrows(SortInvalidException::class.java) { sortParser.parse("not_order:name") }
    }
}
