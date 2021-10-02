package io.github.siyual_park.application.external.converter.mapper

import io.github.siyual_park.application.external.dto.response.CreateTokenResponse
import io.github.siyual_park.auth.domain.token.Tokens
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import org.springframework.stereotype.Component

@Component
class CreateTokenResponseMapper : Mapper<Tokens, CreateTokenResponse> {
    override val sourceType = object : TypeReference<Tokens>() {}
    override val targetType = object : TypeReference<CreateTokenResponse>() {}

    override suspend fun map(source: Tokens) = CreateTokenResponse(
        accessToken = source.accessToken,
        tokenType = source.tokenType,
        expiresIn = source.expiresIn.seconds,
        refreshToken = source.refreshToken
    )
}
