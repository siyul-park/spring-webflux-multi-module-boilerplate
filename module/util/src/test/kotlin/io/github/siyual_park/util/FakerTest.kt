package io.github.siyual_park.util

import com.github.javafaker.Faker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test

class FakerTest {
    private val faker = Faker()

    @Test
    fun username() {
        assertEquals(1, faker.name().username(1).length)
        assertEquals(10, faker.name().username(10).length)
        assertEquals(100, faker.name().username(100).length)
    }

    @Test
    fun word() {
        assertEquals(1, faker.lorem().word(1).length)
        assertEquals(10, faker.lorem().word(10).length)
        assertEquals(100, faker.lorem().word(100).length)
    }

    @Test
    fun url() {
        val url = faker.internet().url(protocol = null)

        assertEquals("http", url.protocol)
        assertNotEquals("", url.host)
        assertNotEquals("", url.path)
    }
}
