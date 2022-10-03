package io.github.siyual_park.mapper

import com.github.javafaker.Faker
import io.github.siyual_park.coroutine.test.CoroutineTestHelper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class MapperContextTest : CoroutineTestHelper() {
    private val mapper = object : Mapper<Int, String> {
        override val sourceType = object : TypeReference<Int>() {}
        override val targetType = object : TypeReference<String>() {}

        override suspend fun map(source: Int): String {
            return source.toString()
        }
    }
    private val faker = Faker()

    @Test
    fun register() {
        MapperContext().apply {
            register(mapper)
        }
    }

    @Test
    fun map() = blocking {
        val context = MapperContext().apply {
            register(mapper)
        }

        faker.random().nextInt(100).let {
            assertEquals(it.toString(), context.map<Int, String>(it))
        }
    }
}
