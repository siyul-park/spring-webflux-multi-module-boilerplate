package io.github.siyual_park

import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
annotation class IntegrationTest
