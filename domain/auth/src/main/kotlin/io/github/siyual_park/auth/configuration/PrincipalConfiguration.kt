package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.domain.principal_refresher.PrincipalRefreshProcessor
import io.github.siyual_park.auth.domain.principal_refresher.PrincipalRefresher
import io.github.siyual_park.auth.domain.token.ClaimEmbedder
import io.github.siyual_park.auth.domain.token.ClaimEmbeddingProcessor
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration

@Configuration
class PrincipalConfiguration(
    private val applicationContext: ApplicationContext
) {
    @Autowired(required = true)
    fun configPrincipalRefresher(principalRefresher: PrincipalRefresher) {
        applicationContext.getBeansOfType(PrincipalRefreshProcessor::class.java).values.forEach {
            principalRefresher.register(it)
        }
    }

    @Autowired(required = true)
    fun configClaimEmbedder(claimEmbedder: ClaimEmbedder) {
        applicationContext.getBeansOfType(ClaimEmbeddingProcessor::class.java).values.forEach {
            claimEmbedder.register(it)
        }
    }
}
