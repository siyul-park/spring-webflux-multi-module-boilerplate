package io.github.siyual_park.validation

import org.hibernate.validator.HibernateValidatorConfiguration
import org.hibernate.validator.internal.engine.DefaultClockProvider
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import javax.validation.ClockProvider
import javax.validation.Configuration
import javax.validation.ConstraintValidator
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class CustomValidatorFactoryBean : LocalValidatorFactoryBean() {
    private val validators = mutableMapOf<KClass<Annotation>, ConstraintValidator<Annotation, Any>>()

    fun <A : Annotation, T : Any> register(annotation: KClass<A>, validator: ConstraintValidator<A, T>) {
        validators[annotation as KClass<Annotation>] = validator as ConstraintValidator<Annotation, Any>
    }

    override fun getClockProvider(): ClockProvider {
        return DefaultClockProvider.INSTANCE
    }

    override fun postProcessConfiguration(configuration: Configuration<*>) {
        val hibernateConfiguration = configuration as? HibernateValidatorConfiguration ?: return

        validators.forEach { (annotation, validator) ->
            val constraintMapping = hibernateConfiguration.createConstraintMapping()

            constraintMapping
                .constraintDefinition(annotation.java)
                .validatedBy(validator.javaClass)
                .includeExistingValidators(true)

            hibernateConfiguration.addMapping(constraintMapping)
        }
    }
}
