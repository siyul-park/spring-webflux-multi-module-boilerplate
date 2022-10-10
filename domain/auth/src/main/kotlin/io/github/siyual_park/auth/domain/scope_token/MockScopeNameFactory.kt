package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.util.word
import net.datafaker.Faker
import java.security.SecureRandom

object MockScopeNameFactory {
    private val faker = Faker(SecureRandom())

    fun create(size: Int = 10, action: String = "test") = "${faker.lorem().word(size - action.length)}:$action"
}
