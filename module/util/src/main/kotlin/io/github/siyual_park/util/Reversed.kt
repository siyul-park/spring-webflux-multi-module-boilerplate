package io.github.siyual_park.util

class Reversed<T>(
    private val original: List<T>
) : Iterable<T> {
    override fun iterator(): Iterator<T> {
        val i = original.listIterator(original.size)
        return object : Iterator<T> {
            override fun hasNext(): Boolean {
                return i.hasPrevious()
            }

            override fun next(): T {
                return i.previous()
            }
        }
    }
}
