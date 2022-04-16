package io.github.siyual_park.client.entity

import io.github.siyual_park.data.AutoModifiable
import io.github.siyual_park.data.Modifiable
import io.github.siyual_park.data.SoftDeletable
import io.github.siyual_park.data.ULIDEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.relational.core.mapping.Table
import java.net.URL
import java.time.Instant

@Table("clients")
data class ClientData(
    @Key
    var name: String,
    val type: ClientType,
    var origin: URL,
    override var deletedAt: Instant? = null
) : ULIDEntity(), Modifiable by AutoModifiable(), SoftDeletable
