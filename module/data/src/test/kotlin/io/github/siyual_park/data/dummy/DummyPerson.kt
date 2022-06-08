package io.github.siyual_park.data.dummy

import com.github.javafaker.Faker
import io.github.siyual_park.data.entity.Person
import io.github.siyual_park.util.resolveNotNull
import io.github.siyual_park.util.username
import java.util.Optional
import kotlin.random.Random

object DummyPerson {
    data class PersonTemplate(
        val name: Optional<String>? = null,
        val age: Optional<Int>? = null,
    )

    private val faker = Faker()

    fun create(template: PersonTemplate? = null): Person {
        return Person(
            name = resolveNotNull(template?.name) { faker.name().username(15) },
            age = resolveNotNull(template?.age) { Random.nextInt() },
        )
    }
}
