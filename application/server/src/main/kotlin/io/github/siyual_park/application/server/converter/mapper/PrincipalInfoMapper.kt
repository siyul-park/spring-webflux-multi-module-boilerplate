package io.github.siyual_park.application.server.converter.mapper

import com.google.common.base.CaseFormat
import io.github.siyual_park.application.server.dto.response.PrincipalInfo
import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.mapper.map
import org.springframework.stereotype.Component

@Component
class PrincipalInfoMapper(
    private val mapperContext: MapperContext
) : Mapper<Principal, PrincipalInfo> {
    override val sourceType = object : TypeReference<Principal>() {}
    override val targetType = object : TypeReference<PrincipalInfo>() {}

    override suspend fun map(source: Principal): PrincipalInfo {
        return PrincipalInfo(
            id = source.id,
            type = CaseFormat.UPPER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, source.javaClass.simpleName),
            scope = mapperContext.map(source = source.scope as Collection<ScopeToken>)
        )
    }
}
