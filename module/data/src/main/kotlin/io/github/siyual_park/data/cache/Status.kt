package io.github.siyual_park.data.cache

data class Status(
    val hit: Long = 0,
    val miss: Long = 0,
    val free: Long? = null,
    val used: Long? = null
) {
    companion object {
        fun add(x: Status, y: Status): Status {
            fun nullableAdd(a: Long?, b: Long?): Long? {
                if (a == null) {
                    return b
                }
                if (b == null) {
                    return a
                }
                return a + b
            }

            return Status(
                hit = x.hit + y.hit,
                miss = x.miss + y.miss,
                free = nullableAdd(x.free, y.free),
                used = nullableAdd(x.used, y.used)
            )
        }
    }
}
