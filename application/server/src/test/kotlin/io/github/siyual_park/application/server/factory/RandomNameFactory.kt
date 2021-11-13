package io.github.siyual_park.application.server.factory

object RandomNameFactory {
    fun create(size: Int) = RandomStringFactory.create(size)
}
