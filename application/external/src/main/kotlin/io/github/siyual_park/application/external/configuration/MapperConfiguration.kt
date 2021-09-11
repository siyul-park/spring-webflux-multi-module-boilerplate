package io.github.siyual_park.application.external.configuration

import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.MapperManager
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class MapperConfiguration(
    private val applicationContext: ApplicationContext
) {
    @Bean
    fun mapperManager(): MapperManager {
        return MapperManager()
    }

    @Autowired(required = true)
    fun configMapperManager(mapperManger: MapperManager) {
        val mappers = applicationContext.getBeansOfType(Mapper::class.java)
        mappers.values.forEach {
            mapperManger.register(it)
        }
    }
}
