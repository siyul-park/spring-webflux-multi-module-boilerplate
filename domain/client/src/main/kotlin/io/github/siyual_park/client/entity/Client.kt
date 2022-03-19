package io.github.siyual_park.client.entity

import io.github.siyual_park.data.SoftDeletable
import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.annotation.Key
import org.springframework.data.relational.core.mapping.Table
import java.net.URL
import java.time.Instant

@Table("clients")
data class Client(
    @Key
    val name: String,
    val type: ClientType,
    var origin: URL,
    override var deletedAt: Instant? = null
) : TimeableEntity<Client, Long>(), SoftDeletable, ClientEntity {
    override val clientId: Long
        get() = id!!

    fun isConfidential(): Boolean {
        return type == ClientType.CONFIDENTIAL
    }

    fun isPublic(): Boolean {
        return type == ClientType.PUBLIC
    }
}
