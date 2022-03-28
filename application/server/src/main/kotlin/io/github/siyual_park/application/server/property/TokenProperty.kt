package io.github.siyual_park.application.server.property

import org.springframework.beans.factory.annotation.Value
import java.time.Duration

data class TokenProperty(
    @Value("#{T(java.time.Duration).ofSeconds(\${application.auth.access-token.age})}")
    val age: Duration,
)
