package io.github.siyual_park.json.bind

import org.springframework.core.Conventions
import org.springframework.core.MethodParameter
import org.springframework.validation.annotation.ValidationAnnotationUtils
import org.springframework.web.bind.support.WebExchangeBindException
import org.springframework.web.reactive.BindingContext
import org.springframework.web.reactive.result.method.HandlerMethodArgumentResolver
import org.springframework.web.server.ServerWebExchange

abstract class HandlerMethodResolverArgumentResolver : HandlerMethodArgumentResolver {
    protected fun extractValidationHints(parameter: MethodParameter): Array<Any>? {
        val annotations = parameter.parameterAnnotations
        for (ann in annotations) {
            val hints = ValidationAnnotationUtils.determineValidationHints(ann)
            if (hints != null) {
                return hints
            }
        }
        return null
    }

    protected fun validate(
        target: Any,
        validationHints: Array<Any>,
        param: MethodParameter,
        binding: BindingContext,
        exchange: ServerWebExchange
    ) {
        val name = Conventions.getVariableNameForParameter(param)
        val binder = binding.createDataBinder(exchange, target, name)
        binder.validate(*validationHints)
        if (binder.bindingResult.hasErrors()) {
            throw WebExchangeBindException(param, binder.bindingResult)
        }
    }
}
