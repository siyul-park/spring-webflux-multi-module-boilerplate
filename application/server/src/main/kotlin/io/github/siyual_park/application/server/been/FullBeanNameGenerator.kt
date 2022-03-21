package io.github.siyual_park.application.server.been

import org.springframework.beans.factory.annotation.AnnotatedBeanDefinition
import org.springframework.beans.factory.config.BeanDefinition
import org.springframework.beans.factory.support.BeanDefinitionRegistry
import org.springframework.beans.factory.support.BeanNameGenerator
import org.springframework.context.annotation.AnnotationBeanNameGenerator

class FullBeanNameGenerator : BeanNameGenerator {
    private val defaultGenerator = AnnotationBeanNameGenerator()

    override fun generateBeanName(definition: BeanDefinition, registry: BeanDefinitionRegistry): String {
        if (definition is AnnotatedBeanDefinition) {
            return generateFullBeanName(definition)
        }

        return defaultGenerator.generateBeanName(definition, registry)
    }

    private fun generateFullBeanName(definition: AnnotatedBeanDefinition): String {
        return definition.metadata.className
    }
}
