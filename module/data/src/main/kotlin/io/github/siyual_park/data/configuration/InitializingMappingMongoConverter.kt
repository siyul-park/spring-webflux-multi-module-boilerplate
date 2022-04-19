package io.github.siyual_park.data.configuration

import org.springframework.beans.factory.InitializingBean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper
import org.springframework.data.mongodb.core.convert.MappingMongoConverter

@Configuration
class InitializingMappingMongoConverter(
    private val mappingMongoConverter: MappingMongoConverter
) : InitializingBean {
    override fun afterPropertiesSet() {
        mappingMongoConverter.setTypeMapper(DefaultMongoTypeMapper(null))
    }
}
