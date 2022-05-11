package io.github.siyual_park.util

import kotlinx.coroutines.sync.Mutex

suspend inline fun <T> Mutex.withLock(func: () -> T): T {
    try {
        lock()
        return func()
    } finally {
        unlock()
    }
}
