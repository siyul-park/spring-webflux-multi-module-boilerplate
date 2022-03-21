package io.github.siyual_park.client.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.relational.core.mapping.Table

@Table("client_credentials")
data class ClientCredentialData(
    @Key
    val clientId: Long,
    var secret: String,
) : TimeableEntity<ClientCredentialData, Long>()