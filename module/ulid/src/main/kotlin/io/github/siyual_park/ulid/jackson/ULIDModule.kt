package io.github.siyual_park.ulid.jackson

import com.fasterxml.jackson.databind.module.SimpleModule
import io.github.siyual_park.ulid.ULID
import org.springframework.stereotype.Component

@Component
class ULIDModule : SimpleModule() {
    init {
        addSerializer(ULID::class.java, ULIDSerializer())
        addDeserializer(ULID::class.java, ULIDDeserializer())
    }
}
