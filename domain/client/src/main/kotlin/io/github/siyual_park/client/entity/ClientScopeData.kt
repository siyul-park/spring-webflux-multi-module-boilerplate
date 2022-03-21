package io.github.siyual_park.client.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.relational.core.mapping.Table

@Table("client_scopes")
data class ClientScopeData(
    @Key("business_keys")
    val clientId: Long,
    @Key("business_keys")
    val scopeTokenId: Long,
) : TimeableEntity<ClientScopeData, Long>()
