package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.entity.TokenData
import io.github.siyual_park.auth.repository.TokenRepository
import io.github.siyual_park.event.EventPublisher
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import org.springframework.stereotype.Component

@Component
class TokenMapper(
    private val tokenRepository: TokenRepository,
    private val scopeTokenStorage: ScopeTokenStorage,
    private val eventPublisher: EventPublisher
) : Mapper<TokenData, Token> {
    override val sourceType = object : TypeReference<TokenData>() {}
    override val targetType = object : TypeReference<Token>() {}

    override suspend fun map(source: TokenData): Token {
        return Token(
            source,
            tokenRepository,
            scopeTokenStorage,
            eventPublisher
        )
    }
}
