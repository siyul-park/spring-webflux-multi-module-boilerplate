package io.github.siyual_park.user.domain

import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.updater.Updater
import io.github.siyual_park.updater.UpdaterAdapter
import io.github.siyual_park.user.entity.User
import io.github.siyual_park.user.repository.UserRepository
import org.springframework.stereotype.Component

@Component
class UserUpdater(
    private val userRepository: UserRepository,
    private val eventPublisher: EventPublisher,
) : Updater<User, Long> by UpdaterAdapter(userRepository, eventPublisher)
