package io.github.siyual_park.data.repository.in_memory.generator

import java.util.concurrent.atomic.AtomicInteger

class IntIdGenerator : IdGenerator<Int> {
    var counter = AtomicInteger(0)

    override fun generate(): Int {
        return counter.incrementAndGet()
    }
}
