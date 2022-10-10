package io.github.siyual_park

import org.junit.jupiter.api.parallel.ResourceLock
import org.springframework.boot.test.autoconfigure.web.reactive.AutoConfigureWebTestClient
import org.springframework.test.context.ActiveProfiles

@SpringBootTest
@AutoConfigureWebTestClient
@ActiveProfiles("test")
@ResourceLock("spring")
annotation class IntegrationTest
