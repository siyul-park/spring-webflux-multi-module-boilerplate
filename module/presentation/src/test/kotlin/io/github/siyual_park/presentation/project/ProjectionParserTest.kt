package io.github.siyual_park.presentation.project

import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import java.util.Optional

class ProjectionParserTest {
    internal data class Parent(
        val thisIsFirstChild: Child,
        val thisIsSecondChild: Child
    )

    internal data class OptionalParent(
        val thisIsFirstChild: Optional<Child>,
        val thisIsSecondChild: Optional<Child>
    )

    internal data class CollectionParent(
        val thisIsFirstChild: Collection<Child>,
        val thisIsSecondChild: Collection<Child>
    )

    internal data class Child(
        val thisIsThirdChild: String,
    )

    private val objectMapper = jacksonObjectMapper().apply {
        propertyNamingStrategy = PropertyNamingStrategies.SNAKE_CASE
    }

    @Test
    fun parse() {
        val projectionParser = ProjectionParser(Parent::class, objectMapper)

        val expect = ProjectNode.Stem().apply {
            set("thisIsFirstChild", ProjectNode.Leaf)
            set(
                "thisIsSecondChild",
                ProjectNode.Stem().apply {
                    set("thisIsThirdChild", ProjectNode.Leaf)
                }
            )
        }
        val projection = listOf("this_is_first_child", "this_is_second_child.this_is_third_child")

        assertEquals(expect, projectionParser.parse(projection))
    }

    @Test
    fun parseWithOptional() {
        val projectionParser = ProjectionParser(OptionalParent::class, objectMapper)

        val expect = ProjectNode.Stem().apply {
            set("thisIsFirstChild", ProjectNode.Leaf)
            set(
                "thisIsSecondChild",
                ProjectNode.Stem().apply {
                    set("thisIsThirdChild", ProjectNode.Leaf)
                }
            )
        }
        val projection = listOf("this_is_first_child", "this_is_second_child.this_is_third_child")

        assertEquals(expect, projectionParser.parse(projection))
    }

    @Test
    fun parseWithCollection() {
        val projectionParser = ProjectionParser(CollectionParent::class, objectMapper)

        val expect = ProjectNode.Stem().apply {
            set("thisIsFirstChild", ProjectNode.Leaf)
            set(
                "thisIsSecondChild",
                ProjectNode.Stem().apply {
                    set("thisIsThirdChild", ProjectNode.Leaf)
                }
            )
        }
        val projection = listOf("this_is_first_child", "this_is_second_child.this_is_third_child")

        assertEquals(expect, projectionParser.parse(projection))
    }
}
