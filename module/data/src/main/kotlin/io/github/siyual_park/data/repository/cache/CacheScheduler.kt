package io.github.siyual_park.data.repository.cache

import java.time.Duration
import java.time.Instant
import java.util.concurrent.atomic.AtomicLong
import kotlin.math.abs
import kotlin.math.ceil
import kotlin.math.floor

class CacheScheduler {
    val cacheSamples = List(5) { AtomicLong(0) }
    val nonCacheSamples = List(5) { AtomicLong(0) }

    private val samplingMin = 1L
    private val samplingMax = 150L

    fun isCacheFaster(size: Int): Boolean {
        return getSample(size, cacheSamples) <= getSample(size, nonCacheSamples)
    }

    private fun getSample(size: Int, sample: List<AtomicLong>): Long {
        val (firstSample, secondSample) = getSamples(size, sample)
        val (firstValue, fistRate) = firstSample
        val (secondValue, secondRate) = secondSample

        val result = firstValue.get() * fistRate + secondValue.get() + secondRate
        if (result == 0L) {
            return Long.MAX_VALUE
        }
        return result
    }

    fun getSamples(size: Int, sample: List<AtomicLong>): Pair<Pair<AtomicLong, Long>, Pair<AtomicLong, Long>> {
        if (size <= samplingMin) {
            return (sample[0] to 100L) to (sample[1] to 0L)
        }
        if (size >= samplingMax) {
            return (sample[sample.size - 2] to 0L) to (sample[sample.size - 1] to 100L)
        }

        val unit = (samplingMax - samplingMin) / sample.size

        val current = ((size - samplingMin).toDouble() / unit)

        val first = floor(current).toInt()
        val second = ceil(current).toInt()

        return (sample[first] to (abs(1 - (current - first)) * 100).toLong()) to (sample[second] to (abs(1 - (current - second)) * 100).toLong())
    }
}

inline fun <T> CacheScheduler.measureCache(size: Int, action: () -> T): T {
    return measure(size, cacheSamples, action)
}

inline fun <T> CacheScheduler.measureNonCache(size: Int, action: () -> T): T {
    return measure(size, nonCacheSamples, action)
}

inline fun <T> CacheScheduler.measure(size: Int, sample: List<AtomicLong>, action: () -> T): T {
    val start = Instant.now()
    try {
        return action()
    } finally {
        val end = Instant.now()
        val diff = Duration.between(start, end)

        val unit = diff.toMillis() / size

        val (firstSample, secondSample) = getSamples(size, sample)
        val (firstValue, fistRate) = firstSample
        val (secondValue, secondRate) = secondSample

        firstValue.updateAndGet { (it * (100 - fistRate * 25 / 100) + unit * fistRate * 25 / 100) / 100 }
        secondValue.updateAndGet { (it * (100 - secondRate * 25 / 100) + unit * secondRate * 25 / 100) / 100 }
    }
}
