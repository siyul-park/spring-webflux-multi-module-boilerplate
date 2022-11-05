package io.github.siyual_park.auth.domain.token

import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.entity.TokenEntity
import io.github.siyual_park.auth.repository.TokenEntityRepository
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import org.springframework.stereotype.Component

@Component
class TokenMapper(
    private val tokenEntityRepository: TokenEntityRepository,
    private val scopeTokenStorage: ScopeTokenStorage
) : Mapper<TokenEntity, Token> {
    override val sourceType = object : TypeReference<TokenEntity>() {}
    override val targetType = object : TypeReference<Token>() {}

    override suspend fun map(source: TokenEntity): Token {
        return Token(
            source,
            tokenEntityRepository,
            scopeTokenStorage,
        )
    }
}
