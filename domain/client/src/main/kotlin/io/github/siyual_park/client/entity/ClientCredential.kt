package io.github.siyual_park.client.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.annotation.Key
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Table

@Table("client_credentials")
data class ClientCredential(
    @Key
    var clientId: Long,
    var secret: String,
) : TimeableEntity<ClientCredential, Long>() {
    override fun clone(): ClientCredential {
        return copyDefaultColumn(this.copy())
    }
}
