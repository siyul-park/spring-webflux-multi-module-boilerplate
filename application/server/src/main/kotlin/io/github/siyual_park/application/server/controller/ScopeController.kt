package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.request.CreateScopeTokenRequest
import io.github.siyual_park.application.server.dto.request.UpdateScopeTokenRequest
import io.github.siyual_park.application.server.dto.response.ScopeTokenInfo
import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.authorization.withAuthorize
import io.github.siyual_park.auth.domain.scope_token.CreateScopeTokenPayload
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFactory
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.entity.ScopeTokenData
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
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.toSet
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
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

@Tag(name = "scope")
@RestController
@RequestMapping("/scope")
class ScopeController(
    private val scopeTokenFactory: ScopeTokenFactory,
    private val scopeTokenStorage: ScopeTokenStorage,
    rhsFilterParserFactory: RHSFilterParserFactory,
    sortParserFactory: SortParserFactory,
    projectionParserFactory: ProjectionParserFactory,
    private val authorizator: Authorizator,
    private val operator: TransactionalOperator,
    private val mapperContext: MapperContext
) {
    private val rhsFilterParser = rhsFilterParserFactory.create(ScopeTokenData::class)
    private val sortParser = sortParserFactory.create(ScopeTokenData::class)
    private val projectionParser = projectionParserFactory.create(ScopeTokenInfo::class)

    private val offsetPaginator = OffsetPaginator(scopeTokenStorage)

    @Operation(security = [SecurityRequirement(name = "Bearer")])
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(
        @Valid @RequestBody request: CreateScopeTokenRequest,
        @RequestParam("fields", required = false) fields: Collection<String>? = null,
    ): ScopeTokenInfo = authorizator.withAuthorize(listOf("scope:create")) {
        val projectionNode = projectionParser.parse(fields)
        val payload = CreateScopeTokenPayload(
            name = request.name,
            description = request.description,
            system = false
        )
        val scopeToken = scopeTokenFactory.create(payload)
        mapperContext.map(Projection(scopeToken, projectionNode))
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
    ): OffsetPage<ScopeTokenInfo> = authorizator.withAuthorize(listOf("scope:read")) {
        val projectionNode = projectionParser.parse(fields)
        val criteria = rhsFilterParser.parse(
            mapOf(
                ScopeTokenData::id to id,
                ScopeTokenData::name to name,
                ScopeTokenData::createdAt to createdAt,
                ScopeTokenData::updatedAt to updatedAt
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
    @GetMapping("/{scope-id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'scope:read')")
    suspend fun read(
        @PathVariable("scope-id") scopeId: ULID,
        @RequestParam("fields", required = false) fields: Collection<String>? = null,
    ): ScopeTokenInfo = authorizator.withAuthorize(listOf("scope:read")) {
        val projectionNode = projectionParser.parse(fields)
        val scopeToken = scopeTokenStorage.loadOrFail(scopeId)
        mapperContext.map(Projection(scopeToken, projectionNode))
    }

    @Operation(security = [SecurityRequirement(name = "Bearer")])
    @PatchMapping("/{scope-id}")
    @ResponseStatus(HttpStatus.OK)
    suspend fun update(
        @PathVariable("scope-id") scopeId: ULID,
        @Valid @RequestBody request: UpdateScopeTokenRequest,
        @RequestParam("fields", required = false) fields: Collection<String>? = null,
    ): ScopeTokenInfo = authorizator.withAuthorize(listOf("scope:update")) {
        operator.executeAndAwait {
            val projectionNode = projectionParser.parse(fields)
            val scopeToken = scopeTokenStorage.loadOrFail(scopeId)

            request.children?.let {
                if (it.isPresent) {
                    syncScope(scopeToken, it.get().let { scopeTokenStorage.load(it) }.toSet())
                } else {
                    val existsScope = scopeToken.children().toSet()
                    existsScope.forEach { scopeToken.revoke(it) }
                }
            }

            val patch = PropertyOverridePatch.of<ScopeToken, UpdateScopeTokenRequest>(request.copy(children = null))
            patch.apply(scopeToken).sync()

            mapperContext.map(Projection(scopeToken, projectionNode))
        }!!
    }

    @Operation(security = [SecurityRequirement(name = "Bearer")])
    @DeleteMapping("/{scope-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(
        @PathVariable("scope-id") scopeId: ULID
    ) = authorizator.withAuthorize(listOf("scope:delete")) {
        val scopeToken = scopeTokenStorage.loadOrFail(scopeId)
        scopeToken.clear()
    }

    suspend fun syncScope(
        parent: ScopeToken,
        scope: Collection<ScopeToken>
    ) = operator.executeAndAwait {
        val existsScope = parent.children().toSet()

        val toGrantScope = scope.filter { !existsScope.contains(it) }
        val toRevokeScope = existsScope.filter { !scope.contains(it) }

        if (toGrantScope.isNotEmpty()) {
            authorizator.withAuthorize(listOf("scope.children:create"), null) {
                toGrantScope.forEach { parent.grant(it) }
            }
        }
        if (toRevokeScope.isNotEmpty()) {
            authorizator.withAuthorize(listOf("scope.children:delete"), null) {
                toRevokeScope.forEach { parent.revoke(it) }
            }
        }
    }
}
