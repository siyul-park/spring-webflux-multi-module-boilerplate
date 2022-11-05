package io.github.siyual_park.client.entity

import io.github.siyual_park.data.ModifiableULIDEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.relational.core.mapping.Table
import java.net.URL

@Table("clients")
data class ClientData(
    @Key
    var name: String,
    var origins: Collection<URL>,
    var secret: String?,
) : ModifiableULIDEntity()
