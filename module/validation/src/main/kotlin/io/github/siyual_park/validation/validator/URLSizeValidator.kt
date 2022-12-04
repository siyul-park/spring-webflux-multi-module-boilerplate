package io.github.siyual_park.validation.validator

import io.github.siyual_park.validation.annotation.ValidateMapping
import jakarta.validation.ConstraintValidator
import jakarta.validation.ConstraintValidatorContext
import jakarta.validation.constraints.Size
import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl
import org.hibernate.validator.internal.metadata.descriptor.ConstraintDescriptorImpl
import org.springframework.stereotype.Component
import java.net.URL

@Suppress("UNCHECKED_CAST", "NAME_SHADOWING")
@Component
@ValidateMapping(Size::class)
class URLSizeValidator : ConstraintValidator<Size, URL> {
    override fun isValid(value: URL, context: ConstraintValidatorContext): Boolean {
        val context = context as? ConstraintValidatorContextImpl ?: return false
        val descriptor = context.constraintDescriptor as? ConstraintDescriptorImpl<Size> ?: return false
        val existsAnnotation = descriptor.annotationDescriptor.annotation

        val max = existsAnnotation.max
        val min = existsAnnotation.min

        val size = value.toString().length

        return size in (min + 1) until max
    }
}
