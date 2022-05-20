package io.github.siyual_park.application.server.converter.mapper

import io.github.siyual_park.application.server.dto.response.UserInfo
import io.github.siyual_park.mapper.Mapper
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.TypeReference
import io.github.siyual_park.mapper.map
import io.github.siyual_park.presentation.project.Projection
import io.github.siyual_park.user.domain.User
import org.springframework.stereotype.Component

@Component
class UserInfosMapper(
    private val mapperContext: MapperContext
) : Mapper<Projection<Collection<User>>, Collection<UserInfo>> {
    override val sourceType = object : TypeReference<Projection<Collection<User>>>() {}
    override val targetType = object : TypeReference<Collection<UserInfo>>() {}

    override suspend fun map(source: Projection<Collection<User>>): Collection<UserInfo> {
        return source.value.map { mapperContext.map(Projection(it, source.node)) }
    }
}
