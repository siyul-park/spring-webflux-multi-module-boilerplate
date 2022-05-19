package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.request.CreateUserRequest
import io.github.siyual_park.application.server.dto.request.UpdateUserRequest
import io.github.siyual_park.application.server.dto.response.ScopeTokenInfo
import io.github.siyual_park.application.server.dto.response.UserInfo
import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.authorization.withAuthorize
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.json.patch.PropertyOverridePatch
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.map
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.presentation.filter.RHSFilterParserFactory
import io.github.siyual_park.presentation.pagination.OffsetPage
import io.github.siyual_park.presentation.pagination.OffsetPaginator
import io.github.siyual_park.presentation.sort.SortParserFactory
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
import kotlinx.coroutines.flow.toSet
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
    private val userFactory: UserFactory,
    private val userStorage: UserStorage,
    private val scopeTokenStorage: ScopeTokenStorage,
    rhsFilterParserFactory: RHSFilterParserFactory,
    sortParserFactory: SortParserFactory,
    private val authorizator: Authorizator,
    private val operator: TransactionalOperator,
    private val mapperContext: MapperContext
) {
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
            perPage = perPage,
            page = page
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
        val user = userStorage.loadOrFail(userId)

        request.password?.let {
            updateCredentials(
                userId,
                it.orElseThrow { throw ValidationException("password is cannot be null") }
            )
        }
        request.scope?.let {
            if (it.isPresent) {
                syncScope(userId, it.get())
            } else {
                val existsScope = user.getScope(deep = false).toSet()
                existsScope.forEach { user.revoke(it) }
            }
        }

        val patch = PropertyOverridePatch.of<User, UpdateUserRequest>(
            request.copy(password = null, scope = null)
        )
        patch.apply(user).sync()

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

    private suspend fun updateCredentials(
        userId: ULID,
        password: String
    ) = authorizator.withAuthorize(
        listOf("users.credential:update", "users[self].credential:update"),
        listOf(null, userId)
    ) {
        val user = userStorage.loadOrFail(userId)
        val credential = user.getCredential()

        credential.setPassword(password)
        credential.sync()
    }

    private suspend fun syncScope(
        userId: ULID,
        scope: Collection<ULID>
    ) = operator.executeAndAwait {
        val user = userStorage.loadOrFail(userId)

        val existsScope = user.getScope(deep = false).toSet()
        val requestScope = scopeTokenStorage.load(scope).toSet()

        val toGrantScope = requestScope.filter { !existsScope.contains(it) }
        val toRevokeScope = existsScope.filter { !requestScope.contains(it) }

        toGrantScope.forEach { grant(userId, it.id) }
        toRevokeScope.forEach { revoke(userId, it.id) }
    }

    private suspend fun grant(
        userId: ULID,
        scopeId: ULID
    ) = authorizator.withAuthorize(listOf("users.scope:create"), null) {
        val user = userStorage.loadOrFail(userId)
        val scopeToken = scopeTokenStorage.loadOrFail(scopeId)
        user.grant(scopeToken)
    }

    private suspend fun revoke(
        userId: ULID,
        scopeId: ULID
    ) = authorizator.withAuthorize(listOf("users.scope:delete"), null) {
        val user = userStorage.loadOrFail(userId)
        val scopeToken = scopeTokenStorage.loadOrFail(scopeId)
        user.revoke(scopeToken)
    }
}
