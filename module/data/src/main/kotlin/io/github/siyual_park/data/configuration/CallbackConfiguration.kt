package io.github.siyual_park.data.configuration

import io.github.siyual_park.data.callback.AfterSaveCallback
import io.github.siyual_park.data.callback.AfterSaveCallbacks
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration

@Configuration
class CallbackConfiguration(
    private val applicationContext: ApplicationContext
) {
    @Autowired(required = true)
    fun configureCallbacks(afterSaveCallbacks: AfterSaveCallbacks) {
        applicationContext.getBeansOfType(AfterSaveCallback::class.java)
            .values
            .forEach { afterSaveCallbacks.register(it) }
    }
}
