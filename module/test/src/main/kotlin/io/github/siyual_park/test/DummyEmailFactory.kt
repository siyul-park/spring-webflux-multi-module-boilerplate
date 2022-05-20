package io.github.siyual_park.test

object DummyEmailFactory {
    fun create(size: Int) = "${DummyNameFactory.create(size - 9)}@test.com"
}
