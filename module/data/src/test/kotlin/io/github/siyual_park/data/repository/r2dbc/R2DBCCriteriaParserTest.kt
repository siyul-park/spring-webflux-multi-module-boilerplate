package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.criteria.and
import io.github.siyual_park.data.criteria.or
import io.github.siyual_park.data.criteria.where
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class R2DBCCriteriaParserTest {
    private data class TestData(
        var name: String,
        var age: Int?,
        var activate: Boolean
    )

    private data class TestCase(
        val query: Criteria,
        val sql: String? = null,
    )

    private val parser = R2DBCCriteriaParser(TestData::class)

    @Test
    fun parse() {
        val testCases = listOf(
            TestCase(
                query = where(TestData::name).not("test"),
                sql = "name != 'test'"
            ),
            TestCase(
                query = where(TestData::name).`is`("test"),
                sql = "name = 'test'"
            ),
            TestCase(
                query = where(TestData::age).between(0..10),
                sql = "age BETWEEN 0 AND 10"
            ),
            TestCase(
                query = where(TestData::age).notBetween(0..10),
                sql = "age NOT BETWEEN 0 AND 10"
            ),
            TestCase(
                query = where(TestData::age).lessThan(0),
                sql = "age < 0"
            ),
            TestCase(
                query = where(TestData::age).lessThanOrEquals(0),
                sql = "age <= 0"
            ),
            TestCase(
                query = where(TestData::age).greaterThan(0),
                sql = "age > 0"
            ),
            TestCase(
                query = where(TestData::age).greaterThanOrEquals(0),
                sql = "age >= 0"
            ),
            TestCase(
                query = where(TestData::age).isNull(),
                sql = "age IS NULL"
            ),
            TestCase(
                query = where(TestData::age).isNotNull(),
                sql = "age IS NOT NULL"
            ),
            TestCase(
                query = where(TestData::name).like("test"),
                sql = "name LIKE 'test'"
            ),
            TestCase(
                query = where(TestData::name).notLike("test"),
                sql = "name NOT LIKE 'test'"
            ),
            TestCase(
                query = where(TestData::name).`in`("test1", "test2"),
                sql = "name IN ('test1', 'test2')"
            ),
            TestCase(
                query = where(TestData::name).notIn("test1", "test2"),
                sql = "name NOT IN ('test1', 'test2')"
            ),
            TestCase(
                query = where(TestData::name).`in`(listOf("test1", "test2")),
                sql = "name IN ('test1', 'test2')"
            ),
            TestCase(
                query = where(TestData::name).notIn(listOf("test1", "test2")),
                sql = "name NOT IN ('test1', 'test2')"
            ),
            TestCase(
                query = where(TestData::activate).isTrue(),
                sql = "activate IS TRUE"
            ),
            TestCase(
                query = where(TestData::activate).isFalse(),
                sql = "activate IS FALSE"
            ),
            TestCase(
                query = where(TestData::name).not("test").and(where(TestData::name).like("test")),
                sql = "name != 'test' AND (name LIKE 'test')"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .and(where(TestData::name).not("test").and(where(TestData::name).like("test"))),
                sql = "name != 'test' AND (name != 'test' AND (name LIKE 'test'))"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .and(where(TestData::name).not("test"))
                    .and(where(TestData::name).like("test")),
                sql = "name != 'test' AND (name != 'test') AND (name LIKE 'test')"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .and(listOf(where(TestData::name).not("test"), where(TestData::name).like("test"))),
                sql = "name != 'test' AND (name != 'test') AND (name LIKE 'test')"
            ),
            TestCase(
                query = where(TestData::name).not("test").or(where(TestData::name).like("test")),
                sql = "name != 'test' OR (name LIKE 'test')"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .or(where(TestData::name).not("test").or(where(TestData::name).like("test"))),
                sql = "name != 'test' OR (name != 'test' OR (name LIKE 'test'))"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .or(where(TestData::name).not("test"))
                    .or(where(TestData::name).like("test")),
                sql = "name != 'test' OR (name != 'test') OR (name LIKE 'test')"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .or(listOf(where(TestData::name).not("test"), where(TestData::name).like("test"))),
                sql = "name != 'test' OR (name != 'test') OR (name LIKE 'test')"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .or(where(TestData::name).not("test").and(where(TestData::name).like("test"))),
                sql = "name != 'test' OR (name != 'test' AND (name LIKE 'test'))"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .and(where(TestData::name).not("test").or(where(TestData::name).like("test"))),
                sql = "name != 'test' AND (name != 'test' OR (name LIKE 'test'))"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .or(where(TestData::name).not("test"))
                    .and(where(TestData::name).like("test")),
                sql = "name != 'test' OR (name != 'test') AND (name LIKE 'test')"
            ),
            TestCase(
                query = where(TestData::name).not("test")
                    .and(where(TestData::name).not("test"))
                    .or(where(TestData::name).like("test")),
                sql = "name != 'test' AND (name != 'test') OR (name LIKE 'test')"
            ),
        )

        testCases.forEach {
            if (it.sql != null) {
                val criteria = parser.parse(it.query)
                assertEquals(it.sql, criteria.toString())
            }
        }
    }
}
