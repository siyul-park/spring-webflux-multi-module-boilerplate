package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.ScopeTokenInfo
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import kotlinx.coroutines.flow.toSet
import org.springframework.stereotype.Component

@Component
class ScopeTokenInfoMapper : Mapper<ScopeToken, ScopeTokenInfo> {
    override val sourceType = object : TypeReference<ScopeToken>() {}
    override val targetType = object : TypeReference<ScopeTokenInfo>() {}

    override suspend fun map(source: ScopeToken): ScopeTokenInfo {
        return map(source, source.relations().toSet())
    }

    suspend fun map(source: ScopeToken, relations: Set<Pair<ScopeToken, ScopeToken>>): ScopeTokenInfo {
        val raw = source.raw()
        return ScopeTokenInfo(
            id = raw.id,
            name = raw.name,
            description = raw.description,
            system = raw.system,
            children = if (source.isPacked()) {
                relations
                    .filter { (parent, _) -> parent.id == source.id }
                    .map { (_, child) -> map(child, relations) }
            } else {
                null
            },
            createdAt = raw.createdAt!!,
            updatedAt = raw.updatedAt
        )
    }
}
