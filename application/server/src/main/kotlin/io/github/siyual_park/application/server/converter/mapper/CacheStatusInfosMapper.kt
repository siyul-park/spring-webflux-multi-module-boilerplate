package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.CacheStatusInfo
import io.github.siyual_park.data.cache.Status
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.mapper.map
import org.springframework.stereotype.Component

@Component
class CacheStatusInfosMapper(
    private val mapperContext: MapperContext
) : Mapper<Map<String, Status>, Map<String, CacheStatusInfo>> {
    override val sourceType = object : TypeReference<Map<String, Status>>() {}
    override val targetType = object : TypeReference<Map<String, CacheStatusInfo>>() {}

    override suspend fun map(source: Map<String, Status>): Map<String, CacheStatusInfo> {
        return source.mapValues { (_, value) -> mapperContext.map(value) }
    }
}
