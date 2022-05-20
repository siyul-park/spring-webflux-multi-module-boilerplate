package io.github.siyual_park.test

import java.util.UUID

object DummyStringFactory {
    fun create(size: Int) = UUID.randomUUID().toString().slice(0..size)
}
