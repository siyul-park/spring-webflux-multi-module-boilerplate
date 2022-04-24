package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.request.CreateClientRequest
import io.github.siyual_park.application.server.dto.request.GrantScopeRequest
import io.github.siyual_park.application.server.dto.request.UpdateClientRequest
import io.github.siyual_park.application.server.dto.response.ClientDetailInfo
import io.github.siyual_park.application.server.dto.response.ClientInfo
import io.github.siyual_park.application.server.dto.response.ScopeTokenInfo
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.client.domain.Client
import io.github.siyual_park.client.domain.ClientFactory
import io.github.siyual_park.client.domain.ClientStorage
import io.github.siyual_park.client.domain.CreateClientPayload
import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.client.entity.ClientEntity
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
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.map
import org.springframework.dao.EmptyResultDataAccessException
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

@Tag(name = "client")
@RestController
@RequestMapping("/clients")
class ClientController(
    private val clientFactory: ClientFactory,
    private val clientStorage: ClientStorage,
    scopeTokenStorage: ScopeTokenStorage,
    rhsFilterParserFactory: RHSFilterParserFactory,
    sortParserFactory: SortParserFactory,
    private val mapperContext: MapperContext
) {
    private val authorizableContoller = AuthorizableContoller(clientStorage, scopeTokenStorage, mapperContext)

    private val rhsFilterParser = rhsFilterParserFactory.createR2dbc(ClientData::class)
    private val sortParser = sortParserFactory.create(ClientData::class)

    private val offsetPaginator = OffsetPaginator(clientStorage)

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(null, 'clients:create')")
    suspend fun create(@Valid @RequestBody request: CreateClientRequest): ClientDetailInfo {
        val payload = CreateClientPayload(
            name = request.name,
            type = request.type,
            origin = request.origin
        )
        val client = clientFactory.create(payload)
        return mapperContext.map(client)
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'clients:read')")
    suspend fun readAll(
        @RequestParam("id", required = false) id: String? = null,
        @RequestParam("name", required = false) name: String? = null,
        @RequestParam("type", required = false) type: String? = null,
        @RequestParam("origin", required = false) origin: String? = null,
        @RequestParam("created-at", required = false) createdAt: String? = null,
        @RequestParam("updated-at", required = false) updatedAt: String? = null,
        @RequestParam("sort", required = false) sort: String? = null,
        @RequestParam("page", required = false) page: Int? = null,
        @RequestParam("per-page", required = false) perPage: Int? = null
    ): OffsetPage<ClientInfo> {
        val criteria = rhsFilterParser.parse(
            mapOf(
                ClientData::id to listOf(id),
                ClientData::name to listOf(name),
                ClientData::type to listOf(type),
                ClientData::origin to listOf(origin),
                ClientData::createdAt to listOf(createdAt),
                ClientData::updatedAt to listOf(updatedAt)
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
    @PreAuthorize("hasPermission(null, 'clients[self]:read')")
    suspend fun readSelf(@AuthenticationPrincipal principal: ClientEntity): ClientInfo {
        return read(principal.clientId ?: throw EmptyResultDataAccessException(1))
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @GetMapping("/{client-id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission({null, #clientId}, {'clients:read', 'clients[self]:read'})")
    suspend fun read(@PathVariable("client-id") clientId: ULID): ClientInfo {
        val client = clientStorage.loadOrFail(clientId)
        return mapperContext.map(client)
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @PatchMapping("/{client-id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission({null, #clientId}, {'clients:update', 'clients[self]:update'})")
    suspend fun update(
        @PathVariable("client-id") clientId: ULID,
        @Valid @RequestBody request: UpdateClientRequest
    ): ClientInfo {
        val patch = PropertyOverridePatch.of<Client, UpdateClientRequest>(request)
        val client = clientStorage.loadOrFail(clientId)
            .let { patch.apply(it) }
            .also { it.sync() }

        return mapperContext.map(client)
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @DeleteMapping("/{client-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission({null, #clientId}, {'clients:delete', 'clients[self]:delete'})")
    suspend fun delete(
        @PathVariable("client-id") clientId: ULID,
    ) {
        val client = clientStorage.loadOrFail(clientId)
        client.clear()
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @GetMapping("/{client-id}/scope")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission({null, #clientId}, {'clients.scope:read', 'clients[self].scope:read'})")
    fun readScope(
        @PathVariable("client-id") clientId: ULID
    ): Flow<ScopeTokenInfo> {
        return flow {
            val client = clientStorage.loadOrFail(clientId)
            emitAll(client.getScope(deep = false))
        }.map { mapperContext.map(it) }
    }
    @Operation(security = [SecurityRequirement(name = "bearer")])

    @PostMapping("/{client-id}/scope")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(null, 'clients.scope:create')")
    suspend fun grantScope(
        @PathVariable("client-id") clientId: ULID,
        @Valid @RequestBody request: GrantScopeRequest
    ): ScopeTokenInfo {
        return authorizableContoller.grantScope(clientId, request)
    }
    @Operation(security = [SecurityRequirement(name = "bearer")])

    @DeleteMapping("/{client-id}/scope/{scope-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'clients.scope:delete')")
    suspend fun revokeScope(
        @PathVariable("client-id") clientId: ULID,
        @PathVariable("scope-id") scopeId: ULID
    ) {
        return authorizableContoller.revokeScope(clientId, scopeId)
    }
}
