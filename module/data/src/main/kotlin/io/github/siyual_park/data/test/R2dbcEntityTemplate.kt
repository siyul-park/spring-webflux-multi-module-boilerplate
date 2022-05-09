package io.github.siyual_park.data.test

import io.r2dbc.spi.ConnectionFactory
import org.springframework.core.convert.converter.Converter
import org.springframework.data.r2dbc.core.DefaultReactiveDataAccessStrategy
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate
import org.springframework.data.r2dbc.dialect.DialectResolver
import org.springframework.r2dbc.core.DatabaseClient

fun createR2dbcEntityTemplate(connectionFactory: ConnectionFactory, converters: Collection<Converter<*, *>>): R2dbcEntityTemplate {
    val dialect = DialectResolver.getDialect(connectionFactory)
    val databaseClient = DatabaseClient.builder().connectionFactory(connectionFactory).bindMarkers(dialect.bindMarkersFactory).build()
    val r2dbcConverter = DefaultReactiveDataAccessStrategy.createConverter(
        dialect,
        converters
    )

    return R2dbcEntityTemplate(
        databaseClient,
        dialect,
        r2dbcConverter
    )
}
