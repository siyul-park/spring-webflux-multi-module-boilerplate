package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.request.CreateUserRequest
import io.github.siyual_park.application.server.dto.request.GrantScopeRequest
import io.github.siyual_park.application.server.dto.request.UpdateUserRequest
import io.github.siyual_park.application.server.dto.response.ScopeTokenInfo
import io.github.siyual_park.application.server.dto.response.UserInfo
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.domain.scope_token.loadOrFail
import io.github.siyual_park.json.patch.PropertyOverridePatch
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.map
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.search.filter.RHSFilterParserFactory
import io.github.siyual_park.search.pagination.OffsetPage
import io.github.siyual_park.search.pagination.OffsetPaginator
import io.github.siyual_park.search.sort.SortParserFactory
import io.github.siyual_park.ulid.ULID
import io.github.siyual_park.user.domain.CreateUserPayload
import io.github.siyual_park.user.domain.User
import io.github.siyual_park.user.domain.UserFactory
import io.github.siyual_park.user.domain.UserStorage
import io.github.siyual_park.user.domain.auth.UserPrincipal
import io.github.siyual_park.user.entity.UserData
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
import org.springframework.security.core.annotation.AuthenticationPrincipal
import org.springframework.transaction.reactive.TransactionalOperator
import org.springframework.transaction.reactive.executeAndAwait
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
import javax.validation.ValidationException

@Tag(name = "user")
@RestController
@RequestMapping("/users")
class UserController(
    private val userContactController: UserContactController,
    private val userFactory: UserFactory,
    private val userStorage: UserStorage,
    scopeTokenStorage: ScopeTokenStorage,
    rhsFilterParserFactory: RHSFilterParserFactory,
    sortParserFactory: SortParserFactory,
    private val operator: TransactionalOperator,
    private val mapperContext: MapperContext
) {
    private val authorizableContoller = AuthorizableContoller(userStorage, scopeTokenStorage, mapperContext)

    private val rhsFilterParser = rhsFilterParserFactory.createR2dbc(UserData::class)
    private val sortParser = sortParserFactory.create(UserData::class)

    private val offsetPaginator = OffsetPaginator(userStorage)

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(null, 'users:create')")
    suspend fun create(@Valid @RequestBody request: CreateUserRequest): UserInfo {
        val payload = CreateUserPayload(
            name = request.name,
            email = request.email,
            password = request.password
        )
        val user = userFactory.create(payload)
        return mapperContext.map(user)
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'users:read')")
    suspend fun readAll(
        @RequestParam("id", required = false) id: String? = null,
        @RequestParam("name", required = false) name: String? = null,
        @RequestParam("created_at", required = false) createdAt: String? = null,
        @RequestParam("updated_at", required = false) updatedAt: String? = null,
        @RequestParam("sort", required = false) sort: String? = null,
        @RequestParam("page", required = false) page: Int? = null,
        @RequestParam("per_page", required = false) perPage: Int? = null
    ): OffsetPage<UserInfo> {
        val criteria = rhsFilterParser.parse(
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

        return offsetPage.mapDataAsync { mapperContext.map(it) }
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @GetMapping("/self")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'users[self]:read')")
    suspend fun readSelf(@AuthenticationPrincipal principal: UserPrincipal): UserInfo {
        return read(principal.userId)
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @GetMapping("/{user-id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission({null, #userId}, {'users:read', 'users[self]:read'})")
    suspend fun read(@PathVariable("user-id") userId: ULID): UserInfo {
        val user = userStorage.loadOrFail(userId)
        return mapperContext.map(user)
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @PatchMapping("/{user-id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission({null, #userId}, {'users:update', 'users[self]:update'})")
    suspend fun update(
        @PathVariable("user-id") userId: ULID,
        @Valid @RequestBody request: UpdateUserRequest,
        @AuthenticationPrincipal principal: UserPrincipal
    ): UserInfo = operator.executeAndAwait {
        request.contact?.let {
            userContactController.update(
                userId,
                it.orElseThrow { throw ValidationException("contact is cannot be null") }
            )
        }
        request.contact = null

        val patch = PropertyOverridePatch.of<User, UpdateUserRequest>(request)
        val user = userStorage.loadOrFail(userId)

        patch.apply(user)
        user.sync()

        mapperContext.map(user)
    }!!

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @DeleteMapping("/{user-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission({null, #userId}, {'users:delete', 'users[self]:delete'})")
    suspend fun delete(@PathVariable("user-id") userId: ULID) {
        val user = userStorage.loadOrFail(userId)
        user.clear()
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @GetMapping("/{user-id}/scope")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission({null, #userId}, {'users.scope:read', 'users[self].scope:read'})")
    fun readScope(
        @PathVariable("user-id") userId: ULID
    ): Flow<ScopeTokenInfo> {
        return flow {
            val user = userStorage.loadOrFail(userId)
            emitAll(user.getScope(deep = false))
        }.map { mapperContext.map(it) }
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @PostMapping("/{user-id}/scope")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(null, 'users.scope:create')")
    suspend fun grantScope(
        @PathVariable("user-id") userId: ULID,
        @Valid @RequestBody request: GrantScopeRequest
    ): ScopeTokenInfo {
        return authorizableContoller.grantScope(userId, request)
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @DeleteMapping("/{user-id}/scope/{scope-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'users.scope:delete')")
    suspend fun revokeScope(
        @PathVariable("user-id") userId: ULID,
        @PathVariable("scope-id") scopeId: ULID
    ) {
        return authorizableContoller.revokeScope(userId, scopeId)
    }
}
