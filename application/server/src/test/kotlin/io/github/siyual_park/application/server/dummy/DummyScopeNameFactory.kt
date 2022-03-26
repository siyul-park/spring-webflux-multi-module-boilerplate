package io.github.siyual_park.application.server.dummy

object DummyScopeNameFactory {
    fun create(size: Int) = "${DummyNameFactory.create(size - 5)}:test"
}
