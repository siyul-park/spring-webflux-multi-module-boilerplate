package io.github.siyual_park.auth.entity

import io.github.siyual_park.data.ModifiableLongIDEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("tokens")
data class TokenData(
    @Key("signature")
    var signature: String,
    var claims: Map<String, Any>,
    var expiredAt: Instant? = null
) : ModifiableLongIDEntity()
