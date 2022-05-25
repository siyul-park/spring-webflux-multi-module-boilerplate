package io.github.siyual_park.data.repository.mongo

import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.criteria.and
import io.github.siyual_park.data.criteria.or
import io.github.siyual_park.data.criteria.where
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

class MongoCriteriaParserTest {
    private data class TestData(
        var name: String,
        var age: Int,
        var activate: Boolean
    )

    private data class TestCase(
        val query: Criteria,
        val sql: String? = null,
    )

    private val parser = MongoCriteriaParser(TestData::class)

    @Test
    fun parse() {
        val testCases = listOf(
            TestCase(
                query = where(TestData::name).not("test"),
                sql = "{\"name\": {\"\$ne\": \"test\"}}"
            ),
            TestCase(
                query = where(TestData::name).`is`("test"),
                sql = "{\"name\": \"test\"}"
            ),
            TestCase(
                query = where(TestData::age).between(0..10),
                sql = "{\"age\": {\"\$gte\": 0, \"\$lte\": 10}}"
            ),
            TestCase(
                query = where(TestData::age).notBetween(0..10),
                sql = "{\"\$or\": [{\"age\": {\"\$lt\": 0}}, {\"age\": {\"\$gt\": 10}}]}"
            ),
            TestCase(
                query = where(TestData::age).lessThan(0),
                sql = "{\"age\": {\"\$lt\": 0}}"
            ),
            TestCase(
                query = where(TestData::age).lessThanOrEquals(0),
                sql = "{\"age\": {\"\$lte\": 0}}"
            ),
            TestCase(
                query = where(TestData::age).greaterThan(0),
                sql = "{\"age\": {\"\$gt\": 0}}"
            ),
            TestCase(
                query = where(TestData::age).greaterThanOrEquals(0),
                sql = "{\"age\": {\"\$gte\": 0}}"
            ),
            TestCase(
                query = where(TestData::age).isNull(),
                sql = "{\"age\": null}"
            ),
            TestCase(
                query = where(TestData::age).isNotNull(),
                sql = "{\"age\": {\"\$ne\": null}}"
            ),
            TestCase(
                query = where(TestData::name).regexp(Pattern.compile("test")),
                sql = "{\"name\": {\"\$regularExpression\": {\"pattern\": \"test\", \"options\": \"\"}}}"
            ),
            TestCase(
                query = where(TestData::name).notRegexp(Pattern.compile("test")),
                sql = "{\"name\": {\"\$not\": {\"\$regularExpression\": {\"pattern\": \"test\", \"options\": \"\"}}}}"
            ),
            TestCase(
                query = where(TestData::name).like("%abc[%]%abc%"),
                sql = "{\"name\": {\"\$regularExpression\": {\"pattern\": \"^.*\\\\Qabc\\\\E\\\\Q%\\\\E.*\\\\Qabc\\\\E.*\$\", \"options\": \"\"}}}"
            ),
            TestCase(
                query = where(TestData::name).notLike("%abc[%]%abc%"),
                sql = "{\"name\": {\"\$not\": {\"\$regularExpression\": {\"pattern\": \"^.*\\\\Qabc\\\\E\\\\Q%\\\\E.*\\\\Qabc\\\\E.*\$\", \"options\": \"\"}}}}"
            ),
            TestCase(
                query = where(TestData::name).`in`("test1", "test2"),
                sql = "{\"name\": {\"\$in\": [\"test1\", \"test2\"]}}"
            ),
            TestCase(
                query = where(TestData::name).notIn("test1", "test2"),
                sql = "{\"name\": {\"\$nin\": [\"test1\", \"test2\"]}}"
            ),
            TestCase(
                query = where(TestData::name).`in`(listOf("test1", "test2")),
                sql = "{\"name\": {\"\$in\": [\"test1\", \"test2\"]}}"
            ),
            TestCase(
                query = where(TestData::name).notIn(listOf("test1", "test2")),
                sql = "{\"name\": {\"\$nin\": [\"test1\", \"test2\"]}}"
            ),
            TestCase(
                query = where(TestData::activate).isTrue(),
                sql = "{\"activate\": true}"
            ),
            TestCase(
                query = where(TestData::activate).isFalse(),
                sql = "{\"activate\": false}"
            ),
            TestCase(
                query = where(TestData::name).not("test").and(where(TestData::name).`is`("test")),
                sql = "{\"\$and\": [{\"name\": {\"\$ne\": \"test\"}}, {\"name\": \"test\"}]}"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .and(where(TestData::name).not("test").and(where(TestData::name).`is`("test"))),
                sql = "{\"\$and\": [{\"name\": {\"\$ne\": \"test\"}}, {\"\$and\": [{\"name\": {\"\$ne\": \"test\"}}, {\"name\": \"test\"}]}]}"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .and(where(TestData::name).not("test"))
                    .and(where(TestData::name).`is`("test")),
                sql = "{\"\$and\": [{\"\$and\": [{\"name\": {\"\$ne\": \"test\"}}, {\"name\": {\"\$ne\": \"test\"}}]}, {\"name\": \"test\"}]}"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .and(listOf(where(TestData::name).not("test"), where(TestData::name).`is`("test"))),
                sql = "{\"\$and\": [{\"name\": {\"\$ne\": \"test\"}}, {\"name\": {\"\$ne\": \"test\"}}, {\"name\": \"test\"}]}"
            ),
            TestCase(
                query = where(TestData::name).not("test").or(where(TestData::name).`is`("test")),
                sql = "{\"\$or\": [{\"name\": {\"\$ne\": \"test\"}}, {\"name\": \"test\"}]}"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .or(where(TestData::name).not("test").or(where(TestData::name).`is`("test"))),
                sql = "{\"\$or\": [{\"name\": {\"\$ne\": \"test\"}}, {\"\$or\": [{\"name\": {\"\$ne\": \"test\"}}, {\"name\": \"test\"}]}]}"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .or(where(TestData::name).not("test"))
                    .or(where(TestData::name).`is`("test")),
                sql = "{\"\$or\": [{\"\$or\": [{\"name\": {\"\$ne\": \"test\"}}, {\"name\": {\"\$ne\": \"test\"}}]}, {\"name\": \"test\"}]}"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .or(listOf(where(TestData::name).not("test"), where(TestData::name).`is`("test"))),
                sql = "{\"\$or\": [{\"name\": {\"\$ne\": \"test\"}}, {\"name\": {\"\$ne\": \"test\"}}, {\"name\": \"test\"}]}"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .or(where(TestData::name).not("test").and(where(TestData::name).`is`("test"))),
                sql = "{\"\$or\": [{\"name\": {\"\$ne\": \"test\"}}, {\"\$and\": [{\"name\": {\"\$ne\": \"test\"}}, {\"name\": \"test\"}]}]}"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .and(where(TestData::name).not("test").or(where(TestData::name).`is`("test"))),
                sql = "{\"\$and\": [{\"name\": {\"\$ne\": \"test\"}}, {\"\$or\": [{\"name\": {\"\$ne\": \"test\"}}, {\"name\": \"test\"}]}]}"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .or(where(TestData::name).not("test"))
                    .and(where(TestData::name).`is`("test")),
                sql = "{\"\$and\": [{\"\$or\": [{\"name\": {\"\$ne\": \"test\"}}, {\"name\": {\"\$ne\": \"test\"}}]}, {\"name\": \"test\"}]}"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .and(where(TestData::name).not("test"))
                    .or(where(TestData::name).`is`("test")),
                sql = "{\"\$or\": [{\"\$and\": [{\"name\": {\"\$ne\": \"test\"}}, {\"name\": {\"\$ne\": \"test\"}}]}, {\"name\": \"test\"}]}"
            ),
        )

        testCases.forEach {
            if (it.sql != null) {
                val criteria = parser.parse(it.query)
                assertEquals(it.sql, criteria?.criteriaObject?.toJson())
            }
        }
    }
}
