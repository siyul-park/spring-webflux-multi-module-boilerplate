package io.github.siyual_park.event

import org.springframework.beans.BeansException
import org.springframework.context.ApplicationContext
import org.springframework.stereotype.Component

@Component
class EventFilterFactory(
    private val applicationContext: ApplicationContext
) {
    fun create(mapping: Subscribe): EventFilter {
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
