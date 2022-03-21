package io.github.siyual_park.persistence.dummy

import io.github.siyual_park.persistence.entity.PersonData
import io.github.siyual_park.util.Presence
import java.util.UUID
import kotlin.random.Random

object DummyPerson {
    data class PersonTemplate(
        val name: Presence<String> = Presence.Empty(),
        val age: Presence<Int> = Presence.Empty(),
    )

    fun create(template: PersonTemplate? = null): PersonData {
        val t = Presence.ofNullable(template)
        return PersonData(
            name = t.flatMap { it.name }.orElseGet { UUID.randomUUID().toString().slice(0..10) },
            age = t.flatMap { it.age }.orElseGet { Random.nextInt() }
        )
    }
}
