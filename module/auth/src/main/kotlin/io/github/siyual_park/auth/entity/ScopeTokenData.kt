package io.github.siyual_park.auth.entity

import io.github.siyual_park.data.ModifiableULIDEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.relational.core.mapping.Table

@Table("scope_tokens")
data class ScopeTokenData(
    @Key
    var name: String,
    var description: String? = null,
    val system: Boolean = true
) : ModifiableULIDEntity()
