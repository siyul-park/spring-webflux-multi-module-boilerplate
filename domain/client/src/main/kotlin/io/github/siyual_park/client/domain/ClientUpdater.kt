package io.github.siyual_park.client.domain

import io.github.siyual_park.client.entity.Client
import io.github.siyual_park.client.repository.ClientRepository
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.updater.Updater
import io.github.siyual_park.updater.UpdaterAdapter
import org.springframework.stereotype.Component

@Component
class ClientUpdater(
    private val clientRepository: ClientRepository,
    private val eventPublisher: EventPublisher,
) : Updater<Client, Long> by UpdaterAdapter(clientRepository, eventPublisher)
