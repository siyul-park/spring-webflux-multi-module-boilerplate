package io.github.siyual_park.mapper

import org.springframework.stereotype.Component

@Suppress("UNCHECKED_CAST")
@Component
class MapperManager {
    private val mappers = mutableMapOf<MappingInfo, Mapper<*, *>>()

    fun <SOURCE : Any, TARGET : Any> register(mapper: Mapper<SOURCE, TARGET>): MapperManager {
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

suspend inline fun <SOURCE : Any, TARGET : Any> MapperManager.map(source: SOURCE): TARGET = map(
    source,
    object : TypeReference<SOURCE>() {},
    object : TypeReference<TARGET>() {}
)
