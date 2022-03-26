package io.github.siyual_park.application.server.dummy

object DummyEmailFactory {
    fun create(size: Int) = "${DummyNameFactory.create(size - 9)}@test.com"
}
