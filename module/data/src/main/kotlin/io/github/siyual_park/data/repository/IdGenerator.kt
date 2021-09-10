package io.github.siyual_park.data.repository

interface IdGenerator<ID> {
    fun generate(): ID
}
