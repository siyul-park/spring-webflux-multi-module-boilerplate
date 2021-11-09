package io.github.siyual_park.application.external.converter.mapper

import io.github.siyual_park.application.external.dto.response.TokenInfo
import io.github.siyual_park.auth.domain.token.Tokens
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import org.springframework.stereotype.Component

@Component
class TokenInfoMapper : Mapper<Tokens, TokenInfo> {
    override val sourceType = object : TypeReference<Tokens>() {}
    override val targetType = object : TypeReference<TokenInfo>() {}

    override suspend fun map(source: Tokens) = TokenInfo(
        accessToken = source.accessToken,
        tokenType = source.tokenType,
        expiresIn = source.expiresIn.seconds,
        refreshToken = source.refreshToken
    )
}
