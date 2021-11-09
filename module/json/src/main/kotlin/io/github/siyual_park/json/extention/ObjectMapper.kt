package io.github.siyual_park.json.extention

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers.Default
import kotlinx.coroutines.async

@Suppress("BlockingMethodInNonBlockingContext")
suspend fun ObjectMapper.writeValueAsStringAsync(value: Any): String {
    return CoroutineScope(Default)
        .async { writeValueAsString(value) }
        .await()
}
