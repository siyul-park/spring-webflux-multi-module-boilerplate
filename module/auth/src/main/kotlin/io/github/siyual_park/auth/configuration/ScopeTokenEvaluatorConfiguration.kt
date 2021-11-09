package io.github.siyual_park.auth.configuration

import io.github.siyual_park.auth.annotation.ScopeTokenFilter
import io.github.siyual_park.auth.domain.scope_token.AllScopeTokenEvaluateFilter
import io.github.siyual_park.auth.domain.scope_token.PrincipalHasScopeTokenEvaluator
import io.github.siyual_park.auth.domain.scope_token.ScopeEvaluator
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenEvaluateFilterAdapter
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenEvaluator
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration

@Configuration
class ScopeTokenEvaluatorConfiguration(
    private val applicationContext: ApplicationContext
) {
    @Autowired(required = true)
    fun configScopeEvaluator(scopeEvaluator: ScopeEvaluator) {
        scopeEvaluator.register(AllScopeTokenEvaluateFilter, PrincipalHasScopeTokenEvaluator)

        applicationContext.getBeansOfType(ScopeTokenEvaluator::class.java)
            .values
            .forEach {
                it.javaClass.annotations.filter { it is ScopeTokenFilter }
                    .forEach { annotation ->
                        val filter = annotation as? ScopeTokenFilter ?: return@forEach
                        scopeEvaluator.register(
                            ScopeTokenEvaluateFilterAdapter(filter),
                            it
                        )
                    }
            }
    }
}
