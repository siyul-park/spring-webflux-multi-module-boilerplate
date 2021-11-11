package io.github.siyual_park.client.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.annotation.Key
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Table

@Table("client_scopes")
data class ClientScope(
    @Key("business_keys")
    var clientId: Long,
    @Key("business_keys")
    var scopeTokenId: Long,
) : TimeableEntity<ClientScope, Long>() {
    override fun clone(): ClientScope {
        return copyDefaultColumn(this.copy())
    }
}
