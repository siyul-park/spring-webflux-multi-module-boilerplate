package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.util.word
import net.datafaker.Faker

object MockScopeNameFactory {
    private val faker = Faker()

    fun create(size: Int = 10, action: String = "test") = "${faker.lorem().word(size - action.length)}:$action"
}
