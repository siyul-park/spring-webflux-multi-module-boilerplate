package io.github.siyual_park.auth.entity

import io.github.siyual_park.data.ModifiableULIDEntity
import org.springframework.data.relational.core.mapping.Table
import java.time.Instant

@Table("tokens")
data class TokenData(
    var claims: Map<String, Any>,
    var expiredAt: Instant? = null
) : ModifiableULIDEntity()
