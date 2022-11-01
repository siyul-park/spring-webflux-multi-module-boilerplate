package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.util.username
import net.datafaker.Faker
import java.security.SecureRandom

object MockScopeNameFactory {
    private val faker = Faker(SecureRandom())

    fun create(size: Int = 16, action: String = "test") = "${faker.name().username(size - action.length)}:$action"
}
