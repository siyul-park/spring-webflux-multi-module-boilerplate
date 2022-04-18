package io.github.siyual_park.data.test

import com.mongodb.reactivestreams.client.MongoClient
import org.springframework.core.convert.converter.Converter
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.core.convert.MappingMongoConverter
import org.springframework.data.mongodb.core.convert.MongoCustomConversions
import org.springframework.data.mongodb.core.mapping.MongoMappingContext

fun createReactiveMongoTemplate(mongoClient: MongoClient, databaseName: String, converters: Collection<Converter<*, *>>): ReactiveMongoTemplate {
    val mongoDatabaseFactory = SimpleReactiveMongoDatabaseFactory(mongoClient, databaseName)

    val mongoConverter = run {
        val conversions = MongoCustomConversions(
            mutableListOf<Converter<*, *>>().also {
                it.addAll(converters)
            }
        )
        val context = MongoMappingContext()
        context.setSimpleTypeHolder(conversions.simpleTypeHolder)
        context.afterPropertiesSet()
        val converter = MappingMongoConverter(ReactiveMongoTemplate.NO_OP_REF_RESOLVER, context)
        converter.setCustomConversions(conversions)
        converter.setCodecRegistryProvider(mongoDatabaseFactory)
        converter.afterPropertiesSet()
        converter
    }

    return ReactiveMongoTemplate(mongoDatabaseFactory, mongoConverter)
}
