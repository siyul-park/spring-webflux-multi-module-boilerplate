package io.github.siyual_park.present.sort

import com.fasterxml.jackson.databind.ObjectMapper
import org.junit.jupiter.api.Test

class SortParserFactoryTest {
    internal data class Person(
        var name: String,
        var age: Int
    )

    private val objectMapper = ObjectMapper()
    private val sortParserFactory = SortParserFactory(objectMapper)

    @Test
    fun create() {
        sortParserFactory.create(Person::class)
    }
}
