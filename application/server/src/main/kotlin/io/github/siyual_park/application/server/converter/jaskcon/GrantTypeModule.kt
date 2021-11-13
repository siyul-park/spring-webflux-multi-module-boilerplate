package io.github.siyual_park.application.server.converter.jaskcon

import com.fasterxml.jackson.databind.module.SimpleModule
import io.github.siyual_park.application.server.dto.GrantType
import org.springframework.stereotype.Component

@Component
class GrantTypeModule : SimpleModule() {
    init {
        addSerializer(GrantType::class.java, GrantTypeSerializer())
        addDeserializer(GrantType::class.java, GrantTypeDeserializer())
    }
}
