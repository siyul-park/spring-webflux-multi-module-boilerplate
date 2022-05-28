package io.github.siyual_park.client.dummy

import io.github.siyual_park.test.DummyStringFactory

object DummyScopeNameFactory {
    fun create(size: Int) = "${DummyStringFactory.create(size - 5)}:test"
}
