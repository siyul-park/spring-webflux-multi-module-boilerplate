package io.github.siyual_park.persistence.dummy

import io.github.siyual_park.persistence.entity.PersonData
import io.github.siyual_park.util.resolveNotNull
import io.github.siyual_park.util.username
import net.datafaker.Faker
import java.util.Optional
import kotlin.random.Random

object DummyPerson {
    data class PersonTemplate(
        val name: Optional<String>? = null,
        val age: Optional<Int>? = null,
    )

    private val faker = Faker()

    fun create(template: PersonTemplate? = null): PersonData {
        return PersonData(
            name = resolveNotNull(template?.name) { faker.name().username(10) },
            age = resolveNotNull(template?.age) { Random.nextInt() },
        )
    }
}
