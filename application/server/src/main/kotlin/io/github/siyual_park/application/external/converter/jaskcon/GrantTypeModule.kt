package io.github.siyual_park.application.external.converter.jaskcon

import com.fasterxml.jackson.databind.module.SimpleModule
import io.github.siyual_park.client.converter.jaskcon.ClientTypeDeserializer
import io.github.siyual_park.client.converter.jaskcon.ClientTypeSerializer
import io.github.siyual_park.client.entity.ClientType
import org.springframework.stereotype.Component

@Component
class GrantTypeModule : SimpleModule() {
    init {
        addSerializer(ClientType::class.java, ClientTypeSerializer())
        addDeserializer(ClientType::class.java, ClientTypeDeserializer())
    }
}
