package io.github.siyual_park.json.bind

import com.fasterxml.jackson.databind.ObjectMapper
import org.springframework.core.MethodParameter
import org.springframework.stereotype.Component
import org.springframework.web.reactive.BindingContext
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.ServerWebInputException
import reactor.core.publisher.Mono

@Component
class RequestFormMethodArgumentResolver(
    private val objectMapper: ObjectMapper
) : HandlerMethodResolverArgumentResolver() {
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
                try {
                    val map = mutableMapOf<String, String?>()
                    it.map { (key, value) ->
                        map[key] = value.firstOrNull()
                    }
                    objectMapper.convertValue(map, parameter.parameterType)
                } catch (e: Exception) {
                    throw ServerWebInputException(e.message ?: "")
                }
            }.doOnNext {
                val hints = extractValidationHints(parameter)
                if (hints != null) {
                    validate(it, hints, parameter, bindingContext, exchange)
                }
            }
    }
}
