package io.github.siyual_park.data.repository

import java.util.concurrent.atomic.AtomicLong

class LongIdGenerator : IdGenerator<Long> {
    var counter = AtomicLong(0)

    override fun generate(): Long {
        return counter.incrementAndGet()
    }
}
