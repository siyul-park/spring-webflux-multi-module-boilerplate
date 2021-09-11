package io.github.siyual_park.application.external.converter.mapper

import io.github.siyual_park.application.external.dto.response.CreateTokenResponse
import io.github.siyual_park.auth.domain.token.Tokens
import io.github.siyual_park.mapper.Mapper
import org.springframework.stereotype.Component

@Component
class CreateTokenResponseMapper : Mapper<Tokens, CreateTokenResponse> {
    override val sourceClazz = Tokens::class
    override val targetClazz = CreateTokenResponse::class

    override suspend fun map(source: Tokens) = CreateTokenResponse(
        accessToken = source.accessToken,
        tokenType = source.tokenType,
        expiresIn = source.expiresIn.seconds,
        refreshToken = source.refreshToken
    )
}
