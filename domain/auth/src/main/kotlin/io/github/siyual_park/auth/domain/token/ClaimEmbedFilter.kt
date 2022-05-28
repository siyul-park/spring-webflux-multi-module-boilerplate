package io.github.siyual_park.auth.domain.token

interface ClaimEmbedFilter {
    fun isSubscribe(principal: Any): Boolean
}
