package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.ScopeTokenInfo
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.toList
import org.springframework.stereotype.Component

@Component
class ScopeTokenInfoMapper : Mapper<ScopeToken, ScopeTokenInfo> {
    override val sourceType = object : TypeReference<ScopeToken>() {}
    override val targetType = object : TypeReference<ScopeTokenInfo>() {}

    override suspend fun map(source: ScopeToken): ScopeTokenInfo {
        val raw = source.raw()
        val children = if (source.isPacked()) {
            source.children()
        } else {
            null
        }

        return ScopeTokenInfo(
            id = raw.id,
            name = raw.name,
            description = raw.description,
            system = raw.system,
            children = children?.map { map(it) }?.toList(),
            createdAt = raw.createdAt!!,
            updatedAt = raw.updatedAt
        )
    }
}
