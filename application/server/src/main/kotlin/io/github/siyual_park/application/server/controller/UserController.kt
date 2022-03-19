package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.request.CreateUserRequest
import io.github.siyual_park.application.server.dto.request.MutableUserData
import io.github.siyual_park.application.server.dto.response.UserInfo
import io.github.siyual_park.json.patch.JsonMergePatch
import io.github.siyual_park.json.patch.PatchConverter
import io.github.siyual_park.json.patch.convert
import io.github.siyual_park.mapper.MapperManager
import io.github.siyual_park.mapper.map
import io.github.siyual_park.reader.filter.RHSFilterParserFactory
import io.github.siyual_park.reader.finder.findByIdOrFail
import io.github.siyual_park.reader.pagination.OffsetPage
import io.github.siyual_park.reader.pagination.OffsetPageQuery
import io.github.siyual_park.reader.sort.SortParserFactory
import io.github.siyual_park.user.domain.CreateUserPayload
import io.github.siyual_park.user.domain.UserFactory
import io.github.siyual_park.user.domain.UserFinder
import io.github.siyual_park.user.domain.UserPaginatorFactory
import io.github.siyual_park.user.domain.UserRemover
import io.github.siyual_park.user.domain.UserUpdater
import io.github.siyual_park.user.domain.auth.UserPrincipal
import io.github.siyual_park.user.domain.auth.UserPrincipalExchanger
import io.github.siyual_park.user.entity.User
import io.swagger.annotations.Api
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.web.bind.annotation.DeleteMapping
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.PatchMapping
import org.springframework.web.bind.annotation.PathVariable
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RequestParam
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController
import javax.validation.Valid

@Api(tags = ["user"])
@RestController
@RequestMapping("/users")
class UserController(
    private val userFactory: UserFactory,
    private val userRemover: UserRemover,
    private val userFinder: UserFinder,
    private val userUpdater: UserUpdater,
    private val userPaginatorFactory: UserPaginatorFactory,
    private val userPrincipalExchanger: UserPrincipalExchanger,
    private val patchConverter: PatchConverter,
    rhsFilterParserFactory: RHSFilterParserFactory,
    sortParserFactory: SortParserFactory,
    private val mapperManager: MapperManager
) {
    private val userRHSFilterParser = rhsFilterParserFactory.create(User::class)
    private val userSortParser = sortParserFactory.create(User::class)

    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(null, 'users:create')")
    suspend fun create(@Valid @RequestBody request: CreateUserRequest): UserInfo {
        val payload = CreateUserPayload(
            name = request.name,
            password = request.password
        )
        val user = userFactory.create(payload)
        return mapperManager.map(user)
    }

    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'users:read')")
    suspend fun readAll(
        @RequestParam("id") id: String? = null,
        @RequestParam("name") name: String? = null,
        @RequestParam("created-at") createdAt: String? = null,
        @RequestParam("updated-at") updatedAt: String? = null,
        @RequestParam("sort") sort: String? = null,
        @RequestParam("page") page: Int = 0,
        @RequestParam("per-page") perPage: Int = 15,
    ): OffsetPage<UserInfo> {
        val criteria = userRHSFilterParser.parseFromProperty(
            mapOf(
                User::id to listOf(id),
                User::name to listOf(name),
                User::createdAt to listOf(createdAt),
                User::updatedAt to listOf(updatedAt)
            )
        )
        val paginator = userPaginatorFactory.create(
            criteria = criteria,
            sort = sort?.let { userSortParser.parse(it) }
        )
        val offsetPage = paginator.paginate(
            OffsetPageQuery(
                page = page,
                perPage = perPage
            )
        )
        return offsetPage.mapDataAsync { mapperManager.map(it) }
    }

    @GetMapping("/self")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'users[self]:read')")
    suspend fun readSelf(@AuthenticationPrincipal principal: UserPrincipal): UserInfo {
        val user = userPrincipalExchanger.exchange(principal)
        return mapperManager.map(user)
    }

    @PatchMapping("/self")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'users[self]:update')")
    suspend fun updateSelf(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody patch: JsonMergePatch<MutableUserData>
    ): UserInfo {
        return userUpdater.updateById(
            principal.userId,
            patchConverter.convert(patch)
        )
            .let { mapperManager.map(it) }
    }

    @DeleteMapping("/self")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'users[self]:delete')")
    suspend fun deleteSelf(@AuthenticationPrincipal principal: UserPrincipal) {
        val user = userPrincipalExchanger.exchange(principal)
        userRemover.remove(user, soft = true)
    }

    @GetMapping("/{user-id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'users:read')")
    suspend fun read(@PathVariable("user-id") userId: Long): UserInfo {
        val user = userFinder.findByIdOrFail(userId)
        return mapperManager.map(user)
    }

    @DeleteMapping("/{user-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'users:delete')")
    suspend fun delete(@PathVariable("user-id") userId: Long) {
        val user = userFinder.findByIdOrFail(userId)
        userRemover.remove(user, soft = true)
    }
}
