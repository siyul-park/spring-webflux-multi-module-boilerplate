package io.github.siyual_park.presentation.filter

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import io.github.siyual_park.presentation.entity.Person
import io.github.siyual_park.presentation.exception.FilterInvalidException
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertThrows
import org.junit.jupiter.api.Test
import kotlin.reflect.KClass
import kotlin.reflect.KProperty1

class MongoRHSFilterParserTest : CoroutineTestHelper() {
    private data class TestCase(
        val query: Map<KProperty1<Person, *>, Collection<String?>>,
        val sql: String? = null,
        val exception: KClass<out Exception>? = null
    )

    private val objectMapper = jacksonObjectMapper()
    private val mongoRHSFilterParser = MongoRHSFilterParser(Person::class, objectMapper)

    @Test
    fun parse() {
        val testCases = listOf(

            TestCase(
                query = mapOf(
                    Person::name to listOf("ne:test"),
                ),
                sql = "{\"name\": {\"\$ne\": \"test\"}}"
            ),
            TestCase(
                query = mapOf(
                    Person::name to listOf("eq:test"),
                ),
                sql = "{\"name\": \"test\"}"
            ),
            TestCase(
                query = mapOf(
                    Person::name to listOf("lk:test"),
                ),
                sql = "{\"name\": {\"\$regularExpression\": {\"pattern\": \"test\", \"options\": \"\"}}}"
            ),
            TestCase(
                query = mapOf(
                    Person::age to listOf("gt:0")
                ),
                sql = "{\"age\": {\"\$gt\": 0}}"
            ),
            TestCase(
                query = mapOf(
                    Person::age to listOf("gte:0")
                ),
                sql = "{\"age\": {\"\$gte\": 0}}"
            ),
            TestCase(
                query = mapOf(
                    Person::age to listOf("lt:0")
                ),
                sql = "{\"age\": {\"\$lt\": 0}}"
            ),
            TestCase(
                query = mapOf(
                    Person::age to listOf("lte:0")
                ),
                sql = "{\"age\": {\"\$lte\": 0}}"
            ),
            TestCase(
                query = mapOf(
                    Person::name to listOf("eq:test"),
                    Person::age to listOf("gte:0")
                ),
                sql = "{\"name\": \"test\", \"\$and\": [{\"age\": {\"\$gte\": 0}}]}"
            ),
            TestCase(
                query = mapOf(
                    Person::age to listOf("not_found:0")
                ),
                exception = FilterInvalidException::class
            ),
            TestCase(
                query = mapOf(
                    Person::age to listOf("eq:invalid")
                ),
                exception = FilterInvalidException::class
            ),
        )

        testCases.forEach {
            if (it.sql != null) {
                val criteria = mongoRHSFilterParser.parse(it.query)
                assertEquals(it.sql, criteria?.criteriaObject?.toJson())
            }
            if (it.exception != null) {
                assertThrows(it.exception.java) { mongoRHSFilterParser.parse(it.query) }
            }
        }
    }
}
