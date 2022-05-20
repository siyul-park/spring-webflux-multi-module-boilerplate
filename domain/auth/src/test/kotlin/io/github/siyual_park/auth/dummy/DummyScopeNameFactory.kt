package io.github.siyual_park.auth.dummy

import io.github.siyual_park.test.DummyStringFactory

object DummyScopeNameFactory {
    fun create(size: Int) = "${DummyStringFactory.create(size - 5)}:test"
}
