package io.github.siyual_park.presentation.project

import com.github.javafaker.Faker
import io.github.siyual_park.util.username
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import java.util.Optional

class ProjectNodeTest {
    internal data class A(
        val a: Optional<String>?,
        val b: Optional<B>?
    )

    internal data class B(
        val b: Optional<String>?,
        val c: Optional<String>?
    )

    private val faker = Faker()

    @Test
    fun from() {
        val expect = ProjectNode.Stem().apply {
            set("a", ProjectNode.Leaf)
            set(
                "b",
                ProjectNode.Stem().apply {
                    set("c", ProjectNode.Leaf)
                }
            )
        }

        val value1 = listOf("a", "b.c")
        assertEquals(expect, ProjectNode.from(value1))

        val value2 = listOf("a")
        assertNotEquals(expect, ProjectNode.from(value2))
    }

    @Test
    fun project() {
        val value = A(
            a = null,
            b = Optional.of(
                B(
                    b = Optional.of(faker.name().username(10)),
                    c = Optional.of(faker.name().username(10))
                )
            )
        )

        val projection1 = listOf("")
        val node1 = ProjectNode.from(projection1)

        assertEquals(null, node1.project(A::a) { value.a?.orElse(null) })
        assertEquals(null, node1.project(A::b) { value.b?.orElse(null) })

        val projection2 = listOf("a")
        val node2 = ProjectNode.from(projection2)

        assertEquals(Optional.empty<String>(), node2.project(A::a) { value.a?.orElse(null) })
        assertEquals(null, node2.project(A::b) { value.b?.orElse(null) })

        val projection3 = listOf("b")
        val node3 = ProjectNode.from(projection3)

        assertEquals(null, node3.project(A::a) { value.a?.orElse(null) })
        assertEquals(value.b, node3.project(A::b) { value.b?.orElse(null) })
    }
}
