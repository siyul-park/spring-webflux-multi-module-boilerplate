package io.github.siyual_park.presentation.filter

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import io.github.siyual_park.data.test.R2DBCTest
import io.github.siyual_park.presentation.entity.Person
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class R2dbcRHSFilterParserTest : R2DBCTest() {
    private val objectMapper = ObjectMapper().registerModule(
        KotlinModule.Builder()
            .withReflectionCacheSize(512)
            .configure(KotlinFeature.NullToEmptyCollection, false)
            .configure(KotlinFeature.NullToEmptyMap, false)
            .configure(KotlinFeature.NullIsSameAsDefault, false)
            .configure(KotlinFeature.SingletonSupport, false)
            .configure(KotlinFeature.StrictNullChecks, false)
            .build()
    )
    private val r2dbcRHSFilterParser = R2dbcRHSFilterParser(Person::class, objectMapper)

    @Test
    fun parse() {
        val criteria = r2dbcRHSFilterParser.parse(
            mapOf(
                Person::name to listOf("eq:test"),
                Person::age to listOf("gte:0")
            )
        )

        assertEquals("(name = 'test') AND (age >= 0)", criteria.toString())
    }
}
