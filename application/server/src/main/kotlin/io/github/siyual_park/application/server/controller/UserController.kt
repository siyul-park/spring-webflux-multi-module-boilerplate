package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.request.CreateUserRequest
import io.github.siyual_park.application.server.dto.request.UpdateUserRequest
import io.github.siyual_park.application.server.dto.response.UserInfo
import io.github.siyual_park.mapper.MapperManager
import io.github.siyual_park.mapper.map
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.reader.filter.RHSFilterParserFactory
import io.github.siyual_park.reader.pagination.OffsetPage
import io.github.siyual_park.reader.pagination.OffsetPaginator
import io.github.siyual_park.reader.sort.SortParserFactory
import io.github.siyual_park.user.domain.CreateUserPayload
import io.github.siyual_park.user.domain.UserFactory
import io.github.siyual_park.user.domain.UserStorage
import io.github.siyual_park.user.domain.auth.UserPrincipal
import io.github.siyual_park.user.entity.UserData
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
    private val userStorage: UserStorage,
    rhsFilterParserFactory: RHSFilterParserFactory,
    sortParserFactory: SortParserFactory,
    private val mapperManager: MapperManager
) {
    private val rhsFilterParser = rhsFilterParserFactory.create(UserData::class)
    private val sortParser = sortParserFactory.create(UserData::class)

    private val offsetPaginator = OffsetPaginator(userStorage)

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
        @RequestParam("id", required = false) id: String? = null,
        @RequestParam("name", required = false) name: String? = null,
        @RequestParam("created-at", required = false) createdAt: String? = null,
        @RequestParam("updated-at", required = false) updatedAt: String? = null,
        @RequestParam("sort", required = false) sort: String? = null,
        @RequestParam("page", required = false) page: Int? = null,
        @RequestParam("per-page", required = false) perPage: Int? = null
    ): OffsetPage<UserInfo> {
        val criteria = rhsFilterParser.parseFromProperty(
            mapOf(
                UserData::id to listOf(id),
                UserData::name to listOf(name),
                UserData::createdAt to listOf(createdAt),
                UserData::updatedAt to listOf(updatedAt)
            )
        )
        val offsetPage = offsetPaginator.paginate(
            criteria = criteria,
            sort = sort?.let { sortParser.parse(it) },
            perPage = perPage ?: 15,
            page = page ?: 0
        )

        return offsetPage.mapDataAsync { mapperManager.map(it) }
    }

    @GetMapping("/self")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'users[self]:read')")
    suspend fun readSelf(@AuthenticationPrincipal principal: UserPrincipal): UserInfo {
        val user = userStorage.loadOrFail(principal.userId)
        return mapperManager.map(user)
    }

    @PatchMapping("/self")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'users[self]:update')")
    suspend fun updateSelf(
        @AuthenticationPrincipal principal: UserPrincipal,
        @Valid @RequestBody request: UpdateUserRequest
    ): UserInfo {
        val user = userStorage.loadOrFail(principal.userId)

        request.name?.ifPresent { user.name = it }

        user.sync()
        return mapperManager.map(user)
    }

    @DeleteMapping("/self")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'users[self]:delete')")
    suspend fun deleteSelf(@AuthenticationPrincipal principal: UserPrincipal) {
        val user = userStorage.loadOrFail(principal.userId)
        user.clear()
    }

    @GetMapping("/{user-id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission({null, #userId}, {'users:read', 'users[self]:read'})")
    suspend fun read(@PathVariable("user-id") userId: Long): UserInfo {
        val user = userStorage.loadOrFail(userId)
        return mapperManager.map(user)
    }

    @PatchMapping("/{user-id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission({null, #userId}, {'users:update', 'users[self]:update'})")
    suspend fun update(
        @PathVariable("user-id") userId: Long,
        @Valid @RequestBody request: UpdateUserRequest
    ): UserInfo {
        val user = userStorage.loadOrFail(userId)

        request.name?.ifPresent { user.name = it }

        user.sync()
        return mapperManager.map(user)
    }

    @DeleteMapping("/{user-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission({null, #userId}, {'users:delete', 'users[self]:delete'})")
    suspend fun delete(@PathVariable("user-id") userId: Long) {
        val user = userStorage.loadOrFail(userId)
        user.clear()
    }
}
