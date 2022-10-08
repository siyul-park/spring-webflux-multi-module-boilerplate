package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.CacheStatusInfo
import io.github.siyual_park.data.cache.Status
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.TypeReference
import org.springframework.stereotype.Component

@Component
class CacheStatusInfoMapper : Mapper<Status, CacheStatusInfo> {
    override val sourceType = object : TypeReference<Status>() {}
    override val targetType = object : TypeReference<CacheStatusInfo>() {}

    override suspend fun map(source: Status): CacheStatusInfo {
        return CacheStatusInfo(
            hit = source.hit,
            miss = source.miss,
            free = source.free,
            used = source.used
        )
    }
}
