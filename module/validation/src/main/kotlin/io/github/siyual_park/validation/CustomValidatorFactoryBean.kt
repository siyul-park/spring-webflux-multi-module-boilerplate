package io.github.siyual_park.validation

import io.github.siyual_park.validation.validator.URLSizeValidator
import org.hibernate.validator.HibernateValidatorConfiguration
import org.hibernate.validator.internal.engine.DefaultClockProvider
import org.springframework.stereotype.Component
import org.springframework.validation.beanvalidation.LocalValidatorFactoryBean
import javax.validation.ClockProvider
import javax.validation.Configuration
import javax.validation.constraints.Size

@Component
class CustomValidatorFactoryBean : LocalValidatorFactoryBean() {
    override fun getClockProvider(): ClockProvider {
        return DefaultClockProvider.INSTANCE
    }

    override fun postProcessConfiguration(configuration: Configuration<*>) {
        val hibernateConfiguration = configuration as? HibernateValidatorConfiguration ?: return
        val constraintMapping = hibernateConfiguration.createConstraintMapping()

        constraintMapping
            .constraintDefinition(Size::class.java)
            .validatedBy(URLSizeValidator::class.java)
            .includeExistingValidators(true)

        hibernateConfiguration.addMapping(constraintMapping)
    }
}
