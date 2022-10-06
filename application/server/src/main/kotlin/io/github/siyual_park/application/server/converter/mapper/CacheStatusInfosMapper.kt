package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.CacheStatusInfo
import io.github.siyual_park.data.cache.Status
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import org.springframework.stereotype.Component

@Component
class CacheStatusInfosMapper : Mapper<Map<String, Status>, Map<String, CacheStatusInfo>> {
    override val sourceType = object : TypeReference<Map<String, Status>>() {}
    override val targetType = object : TypeReference<Map<String, CacheStatusInfo>>() {}

    override suspend fun map(source: Map<String, Status>): Map<String, CacheStatusInfo> {
        return source.mapValues { (_, value) ->
            CacheStatusInfo(
                hit = value.hit,
                miss = value.miss,
                free = value.free,
                used = value.used
            )
        }
    }
}
