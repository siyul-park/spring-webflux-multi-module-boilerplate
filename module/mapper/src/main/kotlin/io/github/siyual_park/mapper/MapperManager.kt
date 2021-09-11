package io.github.siyual_park.mapper

import kotlin.reflect.KClass

class MapperManager {
    private val mappers = mutableMapOf<MappingInfo<*, *>, Mapper<*, *>>()

    fun <SOURCE : Any, TARGET : Any> register(mapper: Mapper<SOURCE, TARGET>): MapperManager {
        mappers[MappingInfo(mapper.sourceClazz.java, mapper.targetClazz.java)] = mapper
        return this
    }

    suspend fun <SOURCE : Any, TARGET : Any> map(source: SOURCE, target: KClass<TARGET>): TARGET {
        val mapper = mappers[MappingInfo(source.javaClass, target.java)] ?: throw CantFoundMapperException()
        mapper as Mapper<SOURCE, TARGET>

        return mapper.map(source)
    }
}
