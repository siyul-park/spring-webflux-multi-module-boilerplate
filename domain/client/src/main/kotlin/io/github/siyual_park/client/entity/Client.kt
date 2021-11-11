package io.github.siyual_park.client.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.annotation.GeneratedValue
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Table
import java.net.URI
import java.time.Instant

@Table("clients")
data class Client(
    val name: String?,
    val grantTypes: Collection<String>,
    val redirectUris: Collection<URI>,
    val tokenEndpointAuthMethod: TokenEndpointAuthMethod,
    @GeneratedValue
    var deletedAt: Instant? = null
) : TimeableEntity<Client, Long>(), ClientEntity {
    override val clientId: Long
        get() = id!!

    override fun clone(): Client {
        return copyDefaultColumn(this.copy())
    }

    fun isConfidential(): Boolean {
        return !isPublic()
    }

    fun isPublic(): Boolean {
        return tokenEndpointAuthMethod == TokenEndpointAuthMethod.NONE
    }

    fun isCanUseGrantType(grantType: String): Boolean {
        return grantTypes.contains(grantType)
    }
}
