package io.github.siyual_park.util

import net.datafaker.Faker
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import java.security.SecureRandom
import java.util.Optional

class OptionalTest {
    private val faker = Faker(SecureRandom())

    @Test
    fun resolveNotNull() {
        faker.random().hex().let {
            assertEquals(it, resolveNotNull(Optional.of(it)) { faker.random().hex() })
        }
        faker.random().hex().let {
            assertThrows<RuntimeException> { resolveNotNull(Optional.empty()) { it } }
        }
        faker.random().hex().let {
            assertEquals(it, resolveNotNull(null) { it })
        }
    }

    @Test
    fun resolve() {
        faker.random().hex().let {
            assertEquals(it, resolve(Optional.of(it)) { faker.random().hex() })
        }
        faker.random().hex().let {
            assertEquals(null, resolve(Optional.empty()) { it })
        }
        faker.random().hex().let {
            assertEquals(it, resolve(null) { it })
        }
    }
}
