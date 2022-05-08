package io.github.siyual_park.mapper

import org.springframework.stereotype.Component
import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
@Component
class MapperContext {
    private val mappers = mutableMapOf<MappingInfo, Mapper<*, *>>()

    fun <SOURCE : Any, TARGET : Any> register(mapper: Mapper<SOURCE, TARGET>): MapperContext {
        mappers[MappingInfo(mapper.sourceType.type, mapper.targetType.type)] = mapper
        return this
    }

    suspend fun <SOURCE : Any, TARGET : Any> map(
        source: SOURCE,
        sourceType: TypeReference<SOURCE>,
        targetType: TypeReference<TARGET>
    ): TARGET {
        val mapper = mappers[MappingInfo(sourceType.type, targetType.type)] ?: throw CantFoundMapperException()
        mapper as Mapper<SOURCE, TARGET>

        return mapper.map(source)
    }

    suspend fun <SOURCE : Any, TARGET : Any> map(
        source: SOURCE,
        sourceType: KClass<SOURCE>,
        targetType: KClass<TARGET>
    ): TARGET {
        val mapper = mappers[MappingInfo(sourceType.java, targetType.java)] ?: throw CantFoundMapperException()
        mapper as Mapper<SOURCE, TARGET>

        return mapper.map(source)
    }
}

suspend inline fun <SOURCE : Any, TARGET : Any> MapperContext.map(source: SOURCE): TARGET = map(
    source,
    object : TypeReference<SOURCE>() {},
    object : TypeReference<TARGET>() {}
)
