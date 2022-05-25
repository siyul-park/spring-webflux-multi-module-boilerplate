package io.github.siyual_park.data.criteria

import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

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
