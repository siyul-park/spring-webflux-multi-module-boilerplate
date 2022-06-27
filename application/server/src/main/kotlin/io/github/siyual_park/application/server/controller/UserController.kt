package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.request.CreateUserRequest
import io.github.siyual_park.application.server.dto.request.UpdateUserRequest
import io.github.siyual_park.application.server.dto.response.UserInfo
import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.authorization.withAuthorize
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.data.patch.PropertyOverridePatch
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.map
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.presentation.filter.RHSFilterParserFactory
import io.github.siyual_park.presentation.pagination.OffsetPage
import io.github.siyual_park.presentation.pagination.OffsetPaginator
import io.github.siyual_park.presentation.pagination.map
import io.github.siyual_park.presentation.project.Projection
import io.github.siyual_park.presentation.project.ProjectionParserFactory
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
import kotlinx.coroutines.flow.toSet
import org.springframework.http.HttpStatus
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
    projectionParserFactory: ProjectionParserFactory,
    private val authorizator: Authorizator,
    private val operator: TransactionalOperator,
    private val mapperContext: MapperContext
) {
    private val rhsFilterParser = rhsFilterParserFactory.create(UserData::class)
    private val sortParser = sortParserFactory.create(UserData::class)
    private val projectionParser = projectionParserFactory.create(UserInfo::class)

    private val offsetPaginator = OffsetPaginator(userStorage)

    @Operation(security = [SecurityRequirement(name = "Bearer")])
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(
        @Valid @RequestBody request: CreateUserRequest,
        @RequestParam("fields", required = false) fields: Collection<String>? = null,
    ): UserInfo = authorizator.withAuthorize(listOf("users:create")) {
        val projectionNode = projectionParser.parse(fields)
        val payload = CreateUserPayload(
            name = request.name,
            email = request.email,
            password = request.password
        )
        val user = userFactory.create(payload)
        mapperContext.map(Projection(user, projectionNode))
    }

    @Operation(security = [SecurityRequirement(name = "Bearer")])
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    suspend fun readAll(
        @RequestParam("id", required = false) id: List<String>? = null,
        @RequestParam("name", required = false) name: List<String>? = null,
        @RequestParam("created_at", required = false) createdAt: List<String>? = null,
        @RequestParam("updated_at", required = false) updatedAt: List<String>? = null,
        @RequestParam("sort", required = false) sort: List<String>? = null,
        @RequestParam("page", required = false) page: Int? = null,
        @RequestParam("per_page", required = false) perPage: Int? = null,
        @RequestParam("fields", required = false) fields: Collection<String>? = null,
    ): OffsetPage<UserInfo> = authorizator.withAuthorize(listOf("users:read")) {
        val projectionNode = projectionParser.parse(fields)
        val criteria = rhsFilterParser.parse(
            mapOf(
                UserData::id to id,
                UserData::name to name,
                UserData::createdAt to createdAt,
                UserData::updatedAt to updatedAt
            )
        )
        val offsetPage = offsetPaginator.paginate(
            criteria = criteria,
            sort = sort?.let { sortParser.parse(it) },
            perPage = perPage,
            page = page
        )

        offsetPage.map { mapperContext.map(Projection(it, projectionNode)) }
    }

    @Operation(security = [SecurityRequirement(name = "Bearer")])
    @GetMapping("/{user-id}")
    @ResponseStatus(HttpStatus.OK)
    suspend fun read(
        @PathVariable("user-id") userId: ULID,
        @RequestParam("fields", required = false) fields: Collection<String>? = null,
    ): UserInfo = authorizator.withAuthorize(listOf("users:read", "users[self]:read"), listOf(null, userId)) {
        val projectionNode = projectionParser.parse(fields)
        val user = userStorage.loadOrFail(userId)
        mapperContext.map(Projection(user, projectionNode))
    }

    @Operation(security = [SecurityRequirement(name = "Bearer")])
    @PatchMapping("/{user-id}")
    @ResponseStatus(HttpStatus.OK)
    suspend fun update(
        @PathVariable("user-id") userId: ULID,
        @Valid @RequestBody request: UpdateUserRequest,
        @AuthenticationPrincipal principal: UserPrincipal,
        @RequestParam("fields", required = false) fields: Collection<String>? = null,
    ): UserInfo = authorizator.withAuthorize(listOf("users:update", "users[self]:update"), listOf(null, userId)) {
        operator.executeAndAwait {
            val projectionNode = projectionParser.parse(fields)
            val user = userStorage.loadOrFail(userId)

            request.password?.let {
                updateCredentials(
                    user,
                    it.orElseThrow { throw ValidationException("password is cannot be null") }
                )
            }
            request.scope?.let {
                if (it.isPresent) {
                    syncScope(user, it.get().let { scopeTokenStorage.load(it) }.toSet())
                } else {
                    val existsScope = user.getScope(deep = false).toSet()
                    existsScope.forEach { user.revoke(it) }
                }
            }

            val patch = PropertyOverridePatch.of<User, UpdateUserRequest>(
                request.copy(password = null, scope = null)
            )
            patch.apply(user).sync()

            mapperContext.map(Projection(user, projectionNode))
        }!!
    }

    @Operation(security = [SecurityRequirement(name = "Bearer")])
    @DeleteMapping("/{user-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(
        @PathVariable("user-id") userId: ULID
    ) = authorizator.withAuthorize(listOf("users:delete", "users[self]:delete"), listOf(null, userId)) {
        val user = userStorage.loadOrFail(userId)
        user.clear()
    }

    private suspend fun updateCredentials(
        user: User,
        password: String
    ) = authorizator.withAuthorize(
        listOf("users.credential:update", "users[self].credential:update"),
        listOf(null, user.id)
    ) {
        val credential = user.getCredential()

        credential.setPassword(password)
        credential.sync()
    }

    private suspend fun syncScope(
        user: User,
        scope: Set<ScopeToken>
    ) = operator.executeAndAwait {
        val existsScope = user.getScope(deep = false).toSet()

        val toGrantScope = scope.filter { !existsScope.contains(it) }
        val toRevokeScope = existsScope.filter { !scope.contains(it) }

        if (toGrantScope.isNotEmpty()) {
            authorizator.withAuthorize(listOf("users.scope:create"), null) {
                toGrantScope.forEach { user.grant(it) }
            }
        }
        if (toRevokeScope.isNotEmpty()) {
            authorizator.withAuthorize(listOf("users.scope:delete"), null) {
                toRevokeScope.forEach { user.revoke(it) }
            }
        }
    }
}
