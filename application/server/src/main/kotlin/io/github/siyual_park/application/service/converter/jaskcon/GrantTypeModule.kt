package io.github.siyual_park.application.service.converter.jaskcon

import com.fasterxml.jackson.databind.module.SimpleModule
import io.github.siyual_park.application.service.dto.GrantType
import org.springframework.stereotype.Component

@Component
class GrantTypeModule : SimpleModule() {
    init {
        addSerializer(GrantType::class.java, GrantTypeSerializer())
        addDeserializer(GrantType::class.java, GrantTypeDeserializer())
    }
}
