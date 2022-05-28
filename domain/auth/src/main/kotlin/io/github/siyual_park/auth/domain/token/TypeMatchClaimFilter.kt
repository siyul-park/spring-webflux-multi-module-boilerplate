package io.github.siyual_park.auth.domain.token

import kotlin.reflect.KClass

class TypeMatchClaimFilter<T : Any>(
    private val clazz: KClass<T>
) : ClaimEmbedFilter {
    override fun isSubscribe(principal: Any): Boolean {
        return clazz.isInstance(principal)
    }
}
