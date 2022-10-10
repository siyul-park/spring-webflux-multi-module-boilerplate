package io.github.siyual_park.util

import net.datafaker.Faker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import java.security.SecureRandom

class FakerTest {
    private val faker = Faker(SecureRandom())

    @Test
    fun username() {
        assertTrue(faker.name().username(null).isNotEmpty())
        assertEquals(1, faker.name().username(1).length)
        assertEquals(10, faker.name().username(10).length)
        assertEquals(100, faker.name().username(100).length)
    }

    @Test
    fun word() {
        assertTrue(faker.lorem().word(null).isNotEmpty())
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
