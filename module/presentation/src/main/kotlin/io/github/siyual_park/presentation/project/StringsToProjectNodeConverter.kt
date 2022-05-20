package io.github.siyual_park.presentation.project

import org.springframework.core.convert.converter.Converter
import org.springframework.stereotype.Component

@Component
class StringsToProjectNodeConverter : Converter<Collection<String>, ProjectNode> {
    override fun convert(source: Collection<String>): ProjectNode {
        return ProjectNode.from(source)
    }
}
