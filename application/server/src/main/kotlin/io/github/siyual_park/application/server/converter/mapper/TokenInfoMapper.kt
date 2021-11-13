package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.TokenInfo
import io.github.siyual_park.auth.domain.token.TokenContainer
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import org.springframework.stereotype.Component

@Component
class TokenInfoMapper : Mapper<TokenContainer, TokenInfo> {
    override val sourceType = object : TypeReference<TokenContainer>() {}
    override val targetType = object : TypeReference<TokenInfo>() {}

    override suspend fun map(source: TokenContainer) = TokenInfo(
        accessToken = source.accessToken.value,
        tokenType = source.accessToken.type,
        expiresIn = source.accessToken.expiresIn,
        refreshToken = source.refreshToken?.value
    )
}
