package io.github.siyual_park.data.criteria

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class CriteriaTest {
    @Test
    fun equals() {
        val a = Criteria.And(listOf(Criteria.Equals("a", "a"), Criteria.Equals("b", "b"), Criteria.Empty))
        val b = Criteria.And(listOf(Criteria.Equals("a", "a"), Criteria.Equals("b", "b"), Criteria.Empty))

        assertEquals(a, b)
    }
}
