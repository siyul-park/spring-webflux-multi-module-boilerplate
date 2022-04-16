package io.github.siyual_park.client.entity

import io.github.siyual_park.data.ModifiableULIDEntity
import io.github.siyual_park.data.SoftDeletable
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
) : ModifiableULIDEntity(), SoftDeletable
