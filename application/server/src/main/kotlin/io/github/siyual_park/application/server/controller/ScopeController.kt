package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.request.CreateScopeTokenRequest
import io.github.siyual_park.application.server.dto.request.GrantScopeRequest
import io.github.siyual_park.application.server.dto.request.UpdateScopeTokenRequest
import io.github.siyual_park.application.server.dto.response.ScopeTokenInfo
import io.github.siyual_park.auth.domain.scope_token.CreateScopeTokenPayload
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenFactory
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.auth.entity.ScopeTokenData
import io.github.siyual_park.json.patch.PropertyOverridePatch
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.map
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.search.filter.RHSFilterParserFactory
import io.github.siyual_park.search.pagination.OffsetPage
import io.github.siyual_park.search.pagination.OffsetPaginator
import io.github.siyual_park.search.sort.SortParserFactory
import io.github.siyual_park.ulid.ULID
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.map
import org.springframework.http.HttpStatus
import org.springframework.security.access.prepost.PreAuthorize
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
    private val mapperContext: MapperContext
) {
    private val authorizableContoller = AuthorizableContoller(scopeTokenStorage, scopeTokenStorage, mapperContext)

    private val rhsFilterParser = rhsFilterParserFactory.createR2dbc(ScopeTokenData::class)
    private val sortParser = sortParserFactory.create(ScopeTokenData::class)

    private val offsetPaginator = OffsetPaginator(scopeTokenStorage)

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(null, 'scope:create')")
    suspend fun create(@Valid @RequestBody request: CreateScopeTokenRequest): ScopeTokenInfo {
        val payload = CreateScopeTokenPayload(
            name = request.name,
            description = request.description,
            system = false
        )
        val scopeToken = scopeTokenFactory.create(payload)
        return mapperContext.map(scopeToken)
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'scope:read')")
    suspend fun readAll(
        @RequestParam("id", required = false) id: String? = null,
        @RequestParam("name", required = false) name: String? = null,
        @RequestParam("created-at", required = false) createdAt: String? = null,
        @RequestParam("updated-at", required = false) updatedAt: String? = null,
        @RequestParam("sort", required = false) sort: String? = null,
        @RequestParam("page", required = false) page: Int? = null,
        @RequestParam("per-page", required = false) perPage: Int? = null
    ): OffsetPage<ScopeTokenInfo> {
        val criteria = rhsFilterParser.parse(
            mapOf(
                ScopeTokenData::id to listOf(id),
                ScopeTokenData::name to listOf(name),
                ScopeTokenData::createdAt to listOf(createdAt),
                ScopeTokenData::updatedAt to listOf(updatedAt)
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
    @GetMapping("/{scope-id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'scope:read')")
    suspend fun read(@PathVariable("scope-id") scopeId: ULID): ScopeTokenInfo {
        val scopeToken = scopeTokenStorage.loadOrFail(scopeId)
        return mapperContext.map(scopeToken)
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @PatchMapping("/{scope-id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'scope:update')")
    suspend fun update(
        @PathVariable("scope-id") scopeId: ULID,
        @Valid @RequestBody request: UpdateScopeTokenRequest
    ): ScopeTokenInfo {
        val patch = PropertyOverridePatch.of<ScopeToken, UpdateScopeTokenRequest>(request)
        val scopeToken = scopeTokenStorage.loadOrFail(scopeId)
            .let { patch.apply(it) }
            .also { it.sync() }

        return mapperContext.map(scopeToken)
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @DeleteMapping("/{scope-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'scope:delete')")
    suspend fun delete(@PathVariable("scope-id") scopeId: ULID) {
        val scopeToken = scopeTokenStorage.loadOrFail(scopeId)
        scopeToken.clear()
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @PostMapping("/{scope-id}/children")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(null, 'scope.children:create')")
    suspend fun grantScope(
        @PathVariable("scope-id") scopeId: ULID,
        @Valid @RequestBody request: GrantScopeRequest
    ): ScopeTokenInfo {
        return authorizableContoller.grantScope(scopeId, request)
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @DeleteMapping("/{scope-id}/children/{child-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'scope.children:delete')")
    suspend fun revokeScope(
        @PathVariable("scope-id") scopeId: ULID,
        @PathVariable("child-id") childId: ULID
    ) {
        return authorizableContoller.revokeScope(scopeId, childId)
    }
}
