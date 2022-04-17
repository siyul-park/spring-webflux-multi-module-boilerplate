package io.github.siyual_park.util

suspend fun <T> retry(count: Int?, func: suspend () -> T): T {
    return try {
        func()
    } catch (e: Exception) {
        if (count == 0) {
            throw e
        }
        retry(count?.let { count - 1 }, func)
    }
}
