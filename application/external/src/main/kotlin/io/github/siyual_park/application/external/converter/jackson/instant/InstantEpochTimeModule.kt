package io.github.siyual_park.application.external.converter.jackson.instant

import com.fasterxml.jackson.databind.module.SimpleModule
import java.time.Instant

class InstantEpochTimeModule : SimpleModule() {
    init {
        addSerializer(Instant::class.java, InstantEpochTimeSerializer())
        addDeserializer(Instant::class.java, InstantEpochTimeDeserializer())
    }
}
