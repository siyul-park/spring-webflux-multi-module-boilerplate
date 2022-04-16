package io.github.siyual_park.json.patch

import com.fasterxml.jackson.databind.ObjectMapper
import com.fasterxml.jackson.databind.PropertyNamingStrategies
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.KotlinFeature
import com.fasterxml.jackson.module.kotlin.KotlinModule
import com.fasterxml.jackson.module.kotlin.SingletonSupport
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test

class PatchTest {

    private val objectMapper = ObjectMapper()
        .registerModule(
            KotlinModule.Builder()
                .withReflectionCacheSize(512)
                .configure(KotlinFeature.NullToEmptyCollection, false)
                .configure(KotlinFeature.NullToEmptyMap, false)
                .configure(KotlinFeature.NullIsSameAsDefault, false)
                .configure(KotlinFeature.SingletonSupport, false)
                .configure(KotlinFeature.StrictNullChecks, false)
                .build()
        )
        .registerModule(JavaTimeModule())
        .setPropertyNamingStrategy(PropertyNamingStrategies.SNAKE_CASE)

    private val jsonPatchFactory = JsonPatchFactory(objectMapper)
    private val jsonMergePatchFactory = JsonMergePatchFactory(objectMapper)

    @Test
    fun patch() {
        val source = Book(
            title = "Goodbye!",
            author = Author(
                givenName = "John",
                familyName = "Doe"
            ),
            phoneNumber = null,
            tags = mutableListOf("example", "sample"),
            content = "This will be unchanged"
        )

        val patch = """
        [
            { "op": "replace", "path": "/title", "value": "Hello!"},
            { "op": "remove", "path": "/author/family_name"},
            { "op": "add", "path": "/phone_number", "value": "+01-123-456-7890"},
            { "op": "replace", "path": "/tags", "value": ["example"]}
        ]
        """.trimIndent()

        val jsonPatch = jsonPatchFactory.create<Any>(patch)
        val target = jsonPatch.apply(source)

        assertEquals(
            Book(
                title = "Hello!",
                author = Author(
                    givenName = "John",
                    familyName = null
                ),
                phoneNumber = "+01-123-456-7890",
                tags = mutableListOf("example"),
                content = "This will be unchanged"
            ),
            target
        )
    }

    @Test
    fun mergePatch() {
        val source = Book(
            title = "Goodbye!",
            author = Author(
                givenName = "John",
                familyName = "Doe"
            ),
            phoneNumber = null,
            tags = mutableListOf("example", "sample"),
            content = "This will be unchanged"
        )

        val patch = """
        {
            "title": "Hello!",
            "author": {
                "family_name": null
            },
            "phone_number": "+01-123-456-7890",
            "tags": ["example"]
        }   
        """.trimIndent()

        val jsonPatch = jsonMergePatchFactory.create<Any>(patch)
        val target = jsonPatch.apply(source)

        assertEquals(
            Book(
                title = "Hello!",
                author = Author(
                    givenName = "John",
                    familyName = null
                ),
                phoneNumber = "+01-123-456-7890",
                tags = mutableListOf("example"),
                content = "This will be unchanged"
            ),
            target
        )
    }
}
