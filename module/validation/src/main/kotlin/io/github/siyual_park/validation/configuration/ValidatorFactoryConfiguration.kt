package io.github.siyual_park.validation.configuration

import io.github.siyual_park.validation.CustomValidatorFactoryBean
import io.github.siyual_park.validation.annotation.ValidateMapping
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import javax.validation.ConstraintValidator
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
@Configuration
class ValidatorFactoryConfiguration(
    private val applicationContext: ApplicationContext
) {
    @Bean
    fun customValidatorFactoryBean(): CustomValidatorFactoryBean {
        return CustomValidatorFactoryBean().also { customValidatorFactoryBean ->
            applicationContext.getBeansOfType(ConstraintValidator::class.java).values.forEach {
                it.javaClass.annotations.filterIsInstance<ValidateMapping>()
                    .forEach { annotation ->
                        customValidatorFactoryBean.register(
                            annotation.annotation as KClass<Annotation>,
                            it as ConstraintValidator<Annotation, Any>
                        )
                    }
            }
        }
    }
}
