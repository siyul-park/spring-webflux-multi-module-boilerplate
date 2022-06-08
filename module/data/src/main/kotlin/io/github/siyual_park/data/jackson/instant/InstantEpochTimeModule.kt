package io.github.siyual_park.data.jackson.instant

import com.fasterxml.jackson.databind.module.SimpleModule
import org.springframework.stereotype.Component
import java.time.Instant

@Component
class InstantEpochTimeModule : SimpleModule() {
    init {
        addSerializer(Instant::class.java, InstantEpochTimeSerializer())
        addDeserializer(Instant::class.java, InstantEpochTimeDeserializer())
    }
}
