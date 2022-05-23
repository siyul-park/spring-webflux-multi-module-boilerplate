package io.github.siyual_park.data

interface Extractor<T : Any, KEY : Any> {
    fun getKey(entity: T): KEY?
}
