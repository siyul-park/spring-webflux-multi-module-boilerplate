package io.github.siyual_park.event

interface EventFilter {
    suspend fun <E : Any> filter(event: E): Boolean
}
