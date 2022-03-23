package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.ScopeTokenInfo
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.mapper.map
import kotlinx.coroutines.flow.map
import org.springframework.stereotype.Component

@Component
class ScopeTokenInfosMapper(
    private val mapperContext: MapperContext
) : Mapper<Collection<ScopeToken>, Collection<ScopeTokenInfo>> {
    override val sourceType = object : TypeReference<Collection<ScopeToken>>() {}
    override val targetType = object : TypeReference<Collection<ScopeTokenInfo>>() {}

    override suspend fun map(source: Collection<ScopeToken>): Collection<ScopeTokenInfo> {
        return source.map { mapperContext.map(it) }
    }
}
