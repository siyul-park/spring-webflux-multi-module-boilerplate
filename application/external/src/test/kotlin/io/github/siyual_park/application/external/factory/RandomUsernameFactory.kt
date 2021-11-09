package io.github.siyual_park.application.external.factory

object RandomUsernameFactory {
    fun create(size: Int) =
        "${RandomStringFactory.create(size / 2)}.${RandomStringFactory.create(size - (size / 2 + 1))}"
}
