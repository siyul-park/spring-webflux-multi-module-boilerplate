package io.github.siyual_park.mapper

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.context.ApplicationContext
import org.springframework.context.annotation.Configuration

@Configuration
class MapperConfiguration(
    private val applicationContext: ApplicationContext,
) {
    @Autowired(required = true)
    fun configMapperManager(mapperManger: MapperContext) {
        val mappers = applicationContext.getBeansOfType(Mapper::class.java)
        mappers.values.forEach {
            mapperManger.register(it)
        }
    }
}
