package io.github.siyual_park.view

import com.fasterxml.jackson.databind.module.SimpleModule
import org.springframework.stereotype.Component

@Component
class AccessLimiterModule(
    accessLimitedViewSerializer: AccessLimitedViewSerializer
) : SimpleModule() {
    init {
        this.addSerializer(AccessLimiter::class.java, accessLimitedViewSerializer)
    }
}
