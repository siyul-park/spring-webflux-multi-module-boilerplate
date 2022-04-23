package io.github.siyual_park.user.domain

import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.persistence.Persistence
import io.github.siyual_park.persistence.proxy
import io.github.siyual_park.persistence.proxyNotNull
import io.github.siyual_park.user.entity.UserContactData
import io.github.siyual_park.user.repository.UserContactRepository

class UserContact(
    value: UserContactData,
    userContactRepository: UserContactRepository,
    eventPublisher: EventPublisher
) : Persistence<UserContactData, Long>(value, userContactRepository, eventPublisher) {
    val id by proxyNotNull(root, UserContactData::id)
    val userId by proxy(root, UserContactData::userId)
    val email by proxy(root, UserContactData::email)
}
