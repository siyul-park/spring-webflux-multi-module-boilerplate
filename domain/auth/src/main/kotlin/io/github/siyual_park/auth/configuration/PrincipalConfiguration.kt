package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.domain.Principal
import io.github.siyual_park.auth.domain.PrincipalMapping
import io.github.siyual_park.auth.domain.authorization.ClaimMapping
import io.github.siyual_park.auth.domain.principal_refresher.PrincipalRefreshStrategy
import io.github.siyual_park.auth.domain.principal_refresher.PrincipalRefresher
import io.github.siyual_park.auth.domain.token.ClaimEmbedder
import io.github.siyual_park.auth.domain.token.ClaimEmbeddingStrategy
import io.github.siyual_park.auth.domain.token.TypeMatchClaimFilter
import org.springframework.beans.BeansException
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration
import org.springframework.core.annotation.AnnotationAwareOrderComparator
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
        applicationContext.getBeansOfType(ClaimEmbeddingStrategy::class.java).values
            .sortedWith(AnnotationAwareOrderComparator.INSTANCE)
            .forEach {
                it.javaClass.annotations.filterIsInstance<ClaimMapping>()
                    .forEach { annotation ->
                        val filter = getFilter(annotation)

                        claimEmbedder.register(filter, it)
                    }
            }
    }

    private fun getFilter(mapping: ClaimMapping): TypeMatchClaimFilter<*> {
        val filterBeen = try {
            applicationContext.getBean(mapping.filterBy.java)
        } catch (e: BeansException) {
            null
        }
        return if (filterBeen is TypeMatchClaimFilter<*>) {
            filterBeen
        } else {
            TypeMatchClaimFilter(mapping.filterBy)
        }
    }
}
