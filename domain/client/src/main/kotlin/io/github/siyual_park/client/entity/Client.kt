package io.github.siyual_park.client.entity

import io.github.siyual_park.data.TimeableEntity
import io.github.siyual_park.data.annotation.GeneratedValue
import io.github.siyual_park.data.annotation.Key
import io.github.siyual_park.data.copyDefaultColumn
import org.springframework.data.relational.core.mapping.Table
import java.net.URL
import java.time.Instant

@Table("clients")
data class Client(
    @Key
    val name: String,
    val type: ClientType,
    var origin: URL,
    @GeneratedValue
    var deletedAt: Instant? = null
) : TimeableEntity<Client, Long>(), ClientEntity {
    override val clientId: Long
        get() = id!!

    override fun clone(): Client {
        return copyDefaultColumn(this.copy())
    }

    fun isConfidential(): Boolean {
        return type == ClientType.CONFIDENTIAL
    }

    fun isPublic(): Boolean {
        return type == ClientType.PUBLIC
    }
}
