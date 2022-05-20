package io.github.siyual_park.application.server.dummy

import io.github.siyual_park.test.DummyNameFactory

object DummyScopeNameFactory {
    fun create(size: Int) = "${DummyNameFactory.create(size - 5)}:test"
}
