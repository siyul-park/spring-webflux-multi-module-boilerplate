package io.github.siyual_park.auth.dummy

object DummyScopeNameFactory {
    fun create(size: Int) = "${DummyStringFactory.create(size - 5)}:test"
}
