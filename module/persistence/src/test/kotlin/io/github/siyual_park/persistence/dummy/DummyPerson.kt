package io.github.siyual_park.persistence.dummy

import io.github.siyual_park.persistence.entity.PersonData
import io.github.siyual_park.test.DummyNameFactory
import io.github.siyual_park.test.resolveNotNull
import java.util.Optional
import kotlin.random.Random

object DummyPerson {
    data class PersonTemplate(
        val name: Optional<String>? = null,
        val age: Optional<Int>? = null,
    )

    fun create(template: PersonTemplate? = null): PersonData {
        return PersonData(
            name = resolveNotNull(template?.name) { DummyNameFactory.create(10) },
            age = resolveNotNull(template?.age) { Random.nextInt() },
        )
    }
}
