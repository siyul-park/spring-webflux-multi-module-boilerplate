package io.github.siyual_park.auth.entity

import io.github.siyual_park.data.AutoModifiable
import io.github.siyual_park.data.Modifiable
import io.github.siyual_park.data.ULIDEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.relational.core.mapping.Table

@Table("scope_tokens")
data class ScopeTokenData(
    @Key
    var name: String,
    var description: String? = null,
    val system: Boolean = true
) : ULIDEntity(), Modifiable by AutoModifiable()
