package io.github.siyual_park.mapper

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class TypeReferenceTest {

    @Test
    fun getType() {
        assertEquals(object : TypeReference<Int>() {}.type, object : TypeReference<Int>() {}.type)
        assertNotEquals(object : TypeReference<Int>() {}.type, object : TypeReference<String>() {}.type)
        assertEquals(object : TypeReference<Collection<Int>>() {}.type, object : TypeReference<Collection<Int>>() {}.type)
        assertNotEquals(object : TypeReference<Collection<Int>>() {}.type, object : TypeReference<Collection<String>>() {}.type)
    }

    @Test
    fun testHashCode() {
        assertEquals(object : TypeReference<Int>() {}.hashCode(), object : TypeReference<Int>() {}.hashCode())
        assertNotEquals(object : TypeReference<Int>() {}.hashCode(), object : TypeReference<String>() {}.hashCode())
        assertEquals(object : TypeReference<Collection<Int>>() {}.hashCode(), object : TypeReference<Collection<Int>>() {}.hashCode())
        assertNotEquals(object : TypeReference<Collection<Int>>() {}.hashCode(), object : TypeReference<Collection<String>>() {}.hashCode())
    }

    @Test
    fun testEquals() {
        assertEquals(object : TypeReference<Int>() {}, object : TypeReference<Int>() {})
        assertNotEquals(object : TypeReference<Int>() {}, object : TypeReference<String>() {})
        assertEquals(object : TypeReference<Collection<Int>>() {}, object : TypeReference<Collection<Int>>() {})
        assertNotEquals(object : TypeReference<Collection<Int>>() {}, object : TypeReference<Collection<String>>() {})
    }
}
