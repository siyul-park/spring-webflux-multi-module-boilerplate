package io.github.siyual_park.mapper

import org.springframework.stereotype.Component
import java.lang.reflect.Type

@Suppress("UNCHECKED_CAST")
@Component
class MapperContext {
    internal data class MappingInfo(
        val source: Type,
        val target: Type
    )

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
}

suspend inline fun <SOURCE : Any, TARGET : Any> MapperContext.map(source: SOURCE): TARGET = map(
    source,
    object : TypeReference<SOURCE>() {},
    object : TypeReference<TARGET>() {}
)
