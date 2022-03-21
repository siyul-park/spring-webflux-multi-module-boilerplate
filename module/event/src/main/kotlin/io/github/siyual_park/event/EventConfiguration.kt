package io.github.siyual_park.event

import org.springframework.beans.BeansException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration

@Configuration
class EventConfiguration(
    private val applicationContext: ApplicationContext
) {
    @Autowired(required = true)
    fun configEventEmitter(eventEmitter: EventEmitter) {
        applicationContext.getBeansOfType(EventConsumer::class.java).values.forEach {
            it.javaClass.annotations.filterIsInstance<Subscribe>()
                .forEach { annotation ->
                    val filter = createFilter(annotation)
                    eventEmitter.on(filter, it)
                }
        }
    }

    private fun createFilter(mapping: Subscribe): EventFilter {
        val filterBeen = try {
            applicationContext.getBean(mapping.filterBy.java)
        } catch (e: BeansException) {
            TypeMatchEventFilter(mapping.filterBy)
        }
        return if (filterBeen is EventFilter) {
            filterBeen
        } else {
            TypeMatchEventFilter(mapping.filterBy)
        }
    }
}
