package io.github.siyual_park.json.bind

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono

@Component
class RequestFormResolver(
    private val objectMapper: ObjectMapper
) : HandlerMethodArgumentResolver {
    override fun supportsParameter(parameter: MethodParameter): Boolean {
        return parameter.hasParameterAnnotation(RequestForm::class.java)
    }

    override fun resolveArgument(
        parameter: MethodParameter,
        bindingContext: BindingContext,
        exchange: ServerWebExchange
    ): Mono<Any> {
        return exchange.formData
            .map {
                val propertyNamingStrategy = objectMapper.propertyNamingStrategy
                val map = mutableMapOf<String, String?>()
                it.map { (key, value) ->
                    if (propertyNamingStrategy is PropertyNamingStrategies.NamingBase) {
                        map[propertyNamingStrategy.translate(key)] = value.firstOrNull()
                    } else {
                        map[key] = value.firstOrNull()
                    }
                }
                val jsonString = objectMapper.writeValueAsString(map)
                objectMapper.readValue(jsonString, parameter.parameterType)
            }
    }
}
