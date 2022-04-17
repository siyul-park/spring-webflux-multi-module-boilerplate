package io.github.siyual_park.util

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.flow
import java.time.Duration
import java.time.Instant

fun tickerFlow(period: Duration, initialDelay: Duration = Duration.ZERO) = flow {
    delay(initialDelay.toMillis())
    while (true) {
        emit(Instant.now())
        delay(period.toMillis())
    }
}
