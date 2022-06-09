package io.github.siyual_park.auth.domain.cors

import org.springframework.context.ApplicationContext
import org.springframework.core.ResolvableType
import org.springframework.security.config.web.server.SecurityWebFiltersOrder
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.web.cors.reactive.CorsProcessor
import org.springframework.web.cors.reactive.DefaultCorsProcessor

class CorsSpec(
    private val context: ApplicationContext? = null,
    source: CorsConfigurationSource? = null
) {
    private var corsFilter: CorsWebFilter? = source?.let { CorsWebFilter(it) }

    fun configure(http: ServerHttpSecurity) {
        val corsFilter = getCorsFilter()
        if (corsFilter != null) {
            http.addFilterAt(corsFilter, SecurityWebFiltersOrder.CORS)
        }
    }

    private fun getCorsFilter(): CorsWebFilter? {
        if (corsFilter != null) {
            return corsFilter
        }
        val source = getBeanOrNull(CorsConfigurationSource::class.java) ?: return null
        val processor = getBeanOrNull(CorsProcessor::class.java) ?: DefaultCorsProcessor()
        corsFilter = CorsWebFilter(source, processor)
        return corsFilter
    }

    private fun <T> getBeanOrNull(beanClass: Class<T>): T? {
        return getBeanOrNull(ResolvableType.forClass(beanClass))
    }

    private fun <T> getBeanOrNull(type: ResolvableType): T? {
        if (this.context == null) {
            return null
        }
        val names = this.context.getBeanNamesForType(type)
        return if (names.size == 1) {
            @Suppress("UNCHECKED_CAST")
            this.context.getBean(names[0]) as? T
        } else null
    }
}
