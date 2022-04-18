package io.github.siyual_park.data.configuration

import io.github.siyual_park.data.transaction.ReactiveChainedTransactionManager
import io.r2dbc.spi.ConnectionFactory
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.AutoConfigureOrder
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass
import org.springframework.context.annotation.Configuration
import org.springframework.core.Ordered
import org.springframework.r2dbc.connection.R2dbcTransactionManager

@Configuration(proxyBeanMethods = false)
@ConditionalOnClass(ReactiveChainedTransactionManager::class, ConnectionFactory::class)
@AutoConfigureOrder(Ordered.LOWEST_PRECEDENCE)
class ChainedTransactionConfiguration {
    @Autowired(required = true)
    fun configuration(
        chainedTransactionManager: ReactiveChainedTransactionManager,
        connectionFactory: ConnectionFactory
    ) {
        chainedTransactionManager.registerTransactionManager(R2dbcTransactionManager(connectionFactory))
    }
}
