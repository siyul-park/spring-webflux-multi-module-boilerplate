package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.PrincipalMapping
import io.github.siyual_park.auth.domain.principal_refresher.PrincipalRefreshProcessor
import io.github.siyual_park.auth.domain.principal_refresher.PrincipalRefresher
import io.github.siyual_park.auth.domain.token.ClaimEmbedder
import io.github.siyual_park.auth.domain.token.ClaimEmbeddingProcessor
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
        applicationContext.getBeansOfType(PrincipalRefreshProcessor::class.java).values.forEach {
            it.javaClass.annotations.filter { it is PrincipalMapping }
                .forEach { annotation ->
                    if (annotation !is PrincipalMapping) return@forEach
                    principalRefresher.register(annotation.clazz as KClass<Principal>, it as PrincipalRefreshProcessor<Principal>)
                }
        }
    }

    @Autowired(required = true)
    fun configClaimEmbedder(claimEmbedder: ClaimEmbedder) {
        applicationContext.getBeansOfType(ClaimEmbeddingProcessor::class.java).values.forEach {
            it.javaClass.annotations.filter { it is PrincipalMapping }
                .forEach { annotation ->
                    if (annotation !is PrincipalMapping) return@forEach
                    claimEmbedder.register(annotation.clazz as KClass<Principal>, it as ClaimEmbeddingProcessor<Principal>)
                }
        }
    }
}
