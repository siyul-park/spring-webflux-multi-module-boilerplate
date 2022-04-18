package io.github.siyual_park.data.configuration

import io.github.siyual_park.data.transaction.ReactiveChainedTransactionManager
import org.springframework.boot.autoconfigure.AutoConfigureBefore
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.boot.autoconfigure.transaction.TransactionAutoConfiguration
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.transaction.ReactiveTransactionManager

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ReactiveTransactionManager::class)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
@AutoConfigureBefore(TransactionAutoConfiguration::class)
class ChainedTransactionManagerAutoConfiguration {
    @Bean
    fun reactiveTransactionManager(): ReactiveChainedTransactionManager {
        return ReactiveChainedTransactionManager()
    }
}
