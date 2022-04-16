package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.PrincipalMapping
import io.github.siyual_park.auth.domain.principal_refresher.PrincipalRefreshStrategy
import io.github.siyual_park.auth.domain.principal_refresher.PrincipalRefresher
import io.github.siyual_park.auth.domain.token.ClaimEmbedder
import io.github.siyual_park.auth.domain.token.ClaimEmbeddingStrategy
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
@Configuration
class PrincipalConfiguration(
    private val applicationContext: ApplicationContext
) {
    @Autowired(required = true)
    fun configPrincipalRefresher(principalRefresher: PrincipalRefresher) {
        applicationContext.getBeansOfType(PrincipalRefreshStrategy::class.java).values.forEach {
            it.javaClass.annotations.filterIsInstance<PrincipalMapping>()
                .forEach { annotation ->
                    principalRefresher.register(annotation.clazz as KClass<Principal>, it as PrincipalRefreshStrategy<Principal>)
                }
        }
    }

    @Autowired(required = true)
    fun configClaimEmbedder(claimEmbedder: ClaimEmbedder) {
        applicationContext.getBeansOfType(ClaimEmbeddingStrategy::class.java).values.forEach {
            it.javaClass.annotations.filterIsInstance<PrincipalMapping>()
                .forEach { annotation ->
                    claimEmbedder.register(annotation.clazz as KClass<Principal>, it as ClaimEmbeddingStrategy<Principal>)
                }
        }
    }
}
