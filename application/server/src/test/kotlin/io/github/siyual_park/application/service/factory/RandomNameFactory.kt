package io.github.siyual_park.application.service.factory

object RandomNameFactory {
    fun create(size: Int) = RandomStringFactory.create(size)
}
