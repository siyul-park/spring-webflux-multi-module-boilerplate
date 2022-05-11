package io.github.siyual_park.auth.entity

import io.github.siyual_park.data.ModifiableULIDEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.mongodb.core.mapping.Document
import java.time.Instant

@Document("tokens")
data class TokenData(
    var type: String,
    @Key("signature")
    var signature: String,
    var claims: Map<String, Any>,
    var expiredAt: Instant? = null
) : ModifiableULIDEntity()
