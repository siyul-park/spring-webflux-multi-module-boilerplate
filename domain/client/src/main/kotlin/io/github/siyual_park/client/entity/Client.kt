package io.github.siyual_park.client.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Table
import java.net.URI

@Table("clients")
data class Client(
    val name: String?,
    val tokenEndpointAuthMethod: TokenEndpointAuthMethod,
    val redirectUris: Collection<URI>
) : TimeableEntity<Client, Long>() {
    override fun clone(): Client {
        return copyDefaultColumn(this.copy())
    }

    fun isConfidential(): Boolean {
        return !isPublic()
    }

    fun isPublic(): Boolean {
        return tokenEndpointAuthMethod == TokenEndpointAuthMethod.NONE
    }
}
