package io.github.siyual_park.client.entity

import io.github.siyual_park.data.AutoModifiable
import io.github.siyual_park.data.Modifiable
import io.github.siyual_park.data.ULIDEntity
import io.github.siyual_park.data.annotation.Key
import io.github.siyual_park.ulid.ULID
import org.springframework.data.relational.core.mapping.Table

@Table("client_scopes")
data class ClientScopeData(
    @Key("business_keys")
    val clientId: ULID,
    @Key("business_keys")
    val scopeTokenId: ULID,
) : ULIDEntity(), Modifiable by AutoModifiable()
