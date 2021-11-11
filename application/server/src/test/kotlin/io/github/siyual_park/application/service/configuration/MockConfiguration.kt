package io.github.siyual_park.application.service.configuration

import io.github.siyual_park.auth.domain.token.SecretGenerator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.annotation.Configuration
import java.util.Properties

@Configuration
class MockConfiguration(
    private val secretGenerator: SecretGenerator
) {
    @Autowired(required = true)
    fun properties(properties: Properties) {
        properties.apply {
            setProperty("application.auth.secret", secretGenerator.generate(256))
        }
    }
}
