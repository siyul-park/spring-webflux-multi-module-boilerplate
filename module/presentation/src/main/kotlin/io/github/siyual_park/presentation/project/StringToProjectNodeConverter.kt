package io.github.siyual_park.presentation.project

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class StringToProjectNodeConverter : Converter<String, ProjectNode> {
    override fun convert(source: String): ProjectNode {
        return ProjectNode.from(listOf(source))
    }
}
