package io.github.siyual_park.client.converter.jaskcon

import com.fasterxml.jackson.databind.module.SimpleModule
import io.github.siyual_park.client.entity.ClientType
import org.springframework.stereotype.Component

@Component
class ClientTypeModule : SimpleModule() {
    init {
        addSerializer(ClientType::class.java, ClientTypeSerializer())
        addDeserializer(ClientType::class.java, ClientTypeDeserializer())
    }
}
