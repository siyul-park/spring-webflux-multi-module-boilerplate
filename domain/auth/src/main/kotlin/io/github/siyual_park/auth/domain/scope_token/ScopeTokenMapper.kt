package io.github.siyual_park.auth.domain.scope_token

import io.github.siyual_park.auth.entity.ScopeTokenEntity
import io.github.siyual_park.auth.repository.ScopeRelationEntityRepository
import io.github.siyual_park.auth.repository.ScopeTokenEntityRepository
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import org.springframework.stereotype.Component

@Component
class ScopeTokenMapper(
    private val scopeTokenEntityRepository: ScopeTokenEntityRepository,
    private val scopeRelationEntityRepository: ScopeRelationEntityRepository
) : Mapper<ScopeTokenEntity, ScopeToken> {
    override val sourceType = object : TypeReference<ScopeTokenEntity>() {}
    override val targetType = object : TypeReference<ScopeToken>() {}

    override suspend fun map(source: ScopeTokenEntity): ScopeToken {
        return ScopeToken(
            source,
            scopeTokenEntityRepository,
            scopeRelationEntityRepository
        )
    }
}
