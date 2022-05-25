package io.github.siyual_park.data.criteria

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.util.regex.Pattern

class CriteriaRunnerParserTest {
    data class TestData(
        var name: String? = null,
        var age: Int? = null,
        var activate: Boolean? = null
    )

    private data class TestCase(
        val query: Criteria,
        val expectTrue: List<TestData>,
        val expectFalse: List<TestData>,
    )

    private val parser = CriteriaRunnerParser(TestData::class)

    @Test
    fun parse() {
        val testCases = listOf(
            TestCase(
                query = where(TestData::name).not("test"),
                expectTrue = listOf(TestData(name = "!test"), TestData(name = null)),
                expectFalse = listOf(TestData(name = "test")),
            ),
            TestCase(
                query = where(TestData::name).`is`("test"),
                expectTrue = listOf(TestData(name = "test")),
                expectFalse = listOf(TestData(name = "!test"), TestData(name = null)),
            ),
            TestCase(
                query = where(TestData::age).between(0..10),
                expectTrue = listOf(TestData(age = 0), TestData(age = 5), TestData(age = 10)),
                expectFalse = listOf(TestData(age = -1), TestData(age = 11), TestData(age = null)),
            ),
            TestCase(
                query = where(TestData::age).notBetween(0..10),
                expectTrue = listOf(TestData(age = -1), TestData(age = 11), TestData(age = null)),
                expectFalse = listOf(TestData(age = 0), TestData(age = 5), TestData(age = 10)),
            ),
            TestCase(
                query = where(TestData::age).lessThan(0),
                expectTrue = listOf(TestData(age = -1)),
                expectFalse = listOf(TestData(age = 0), TestData(age = null)),
            ),
            TestCase(
                query = where(TestData::age).lessThanOrEquals(0),
                expectTrue = listOf(TestData(age = 0)),
                expectFalse = listOf(TestData(age = 1), TestData(age = null)),
            ),
            TestCase(
                query = where(TestData::age).greaterThan(0),
                expectTrue = listOf(TestData(age = 1)),
                expectFalse = listOf(TestData(age = 0), TestData(age = null)),
            ),
            TestCase(
                query = where(TestData::age).greaterThanOrEquals(0),
                expectTrue = listOf(TestData(age = 0)),
                expectFalse = listOf(TestData(age = -1), TestData(age = null)),
            ),
            TestCase(
                query = where(TestData::age).isNull(),
                expectTrue = listOf(TestData(age = null)),
                expectFalse = listOf(TestData(age = 0)),
            ),
            TestCase(
                query = where(TestData::age).isNotNull(),
                expectTrue = listOf(TestData(age = 0)),
                expectFalse = listOf(TestData(age = null)),
            ),
            TestCase(
                query = where(TestData::name).like("%test%"),
                expectTrue = listOf(TestData(name = "testtesttest")),
                expectFalse = listOf(TestData(name = "any"), TestData(name = null)),
            ),
            TestCase(
                query = where(TestData::name).notLike("%test%"),
                expectTrue = listOf(TestData(name = "any")),
                expectFalse = listOf(TestData(name = "testtesttest"), TestData(name = null)),
            ),
            TestCase(
                query = where(TestData::name).regexp(Pattern.compile(".*test.*")),
                expectTrue = listOf(TestData(name = "testtesttest")),
                expectFalse = listOf(TestData(name = "any"), TestData(name = null)),
            ),
            TestCase(
                query = where(TestData::name).notRegexp(Pattern.compile(".*test.*")),
                expectTrue = listOf(TestData(name = "any")),
                expectFalse = listOf(TestData(name = "testtesttest"), TestData(name = null)),
            ),
            TestCase(
                query = where(TestData::name).`in`("test1", "test2"),
                expectTrue = listOf(TestData(name = "test1"), TestData(name = "test2")),
                expectFalse = listOf(TestData(name = "any"), TestData(name = null)),
            ),
            TestCase(
                query = where(TestData::name).notIn("test1", "test2"),
                expectTrue = listOf(TestData(name = "any")),
                expectFalse = listOf(TestData(name = "test1"), TestData(name = "test2"), TestData(name = null)),
            ),
            TestCase(
                query = where(TestData::activate).isTrue(),
                expectTrue = listOf(TestData(activate = true)),
                expectFalse = listOf(TestData(activate = false), TestData(activate = null)),
            ),
            TestCase(
                query = where(TestData::activate).isFalse(),
                expectTrue = listOf(TestData(activate = false)),
                expectFalse = listOf(TestData(activate = true), TestData(activate = null)),
            ),
            TestCase(
                query = where(TestData::activate).isTrue().and(where(TestData::name).`is`("test")),
                expectTrue = listOf(TestData(activate = true, name = "test")),
                expectFalse = listOf(TestData(activate = false, name = "test"), TestData(activate = true, name = "!test")),
            ),
            TestCase(
                query = where(TestData::activate).isTrue().or(where(TestData::name).`is`("test")),
                expectTrue = listOf(TestData(activate = true, name = "test"), TestData(activate = false, name = "test"), TestData(activate = true, name = "!test")),
                expectFalse = listOf(TestData(activate = false, name = "!test")),
            ),
        )

        testCases.forEach {
            val criteria = parser.parse(it.query)
            it.expectTrue.forEach {
                assertTrue(criteria?.invoke(it) ?: false)
            }
            it.expectFalse.forEach {
                assertFalse(criteria?.invoke(it) ?: true)
            }
        }
    }
}
