package io.github.siyual_park.util

import java.util.Optional

inline fun <T> resolveNotNull(optional: Optional<T>?, value: () -> T): T {
    return resolve(optional, value) ?: throw RuntimeException("it must be present.")
}

inline fun <T> resolve(optional: Optional<T>?, value: () -> T?): T? {
    if (optional == null) {
        return value()
    }
    if (optional.isPresent) {
        return optional.get()
    }

    return null
}
