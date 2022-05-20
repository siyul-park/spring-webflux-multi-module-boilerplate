package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.request.CreateClientRequest
import io.github.siyual_park.application.server.dto.request.UpdateClientRequest
import io.github.siyual_park.application.server.dto.response.ClientDetailInfo
import io.github.siyual_park.application.server.dto.response.ClientInfo
import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.authorization.withAuthorize
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.client.domain.Client
import io.github.siyual_park.client.domain.ClientFactory
import io.github.siyual_park.client.domain.ClientStorage
import io.github.siyual_park.client.domain.CreateClientPayload
import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.data.patch.PropertyOverridePatch
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.map
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.presentation.filter.RHSFilterParserFactory
import io.github.siyual_park.presentation.pagination.OffsetPage
import io.github.siyual_park.presentation.pagination.OffsetPaginator
import io.github.siyual_park.presentation.project.Projection
import io.github.siyual_park.presentation.project.ProjectionParserFactory
import io.github.siyual_park.presentation.sort.SortParserFactory
import io.github.siyual_park.ulid.ULID
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import kotlinx.coroutines.flow.toSet
import org.springframework.dao.EmptyResultDataAccessException
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

@Tag(name = "client")
@RestController
@RequestMapping("/clients")
class ClientController(
    private val clientFactory: ClientFactory,
    private val clientStorage: ClientStorage,
    private val scopeTokenStorage: ScopeTokenStorage,
    rhsFilterParserFactory: RHSFilterParserFactory,
    sortParserFactory: SortParserFactory,
    projectionParserFactory: ProjectionParserFactory,
    private val authorizator: Authorizator,
    private val operator: TransactionalOperator,
    private val mapperContext: MapperContext
) {
    private val rhsFilterParser = rhsFilterParserFactory.createR2dbc(ClientData::class)
    private val sortParser = sortParserFactory.create(ClientData::class)
    private val projectionParser = projectionParserFactory.create(ClientInfo::class)

    private val offsetPaginator = OffsetPaginator(clientStorage)

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasPermission(null, 'clients:create')")
    suspend fun create(
        @Valid @RequestBody request: CreateClientRequest,
        @RequestParam("fields", required = false) fields: Collection<String>? = null,
    ): ClientDetailInfo {
        val payload = CreateClientPayload(
            name = request.name,
            type = request.type,
            origin = request.origin
        )
        val client = clientFactory.create(payload)
        return mapperContext.map(Projection(client, projectionParser.parse(fields)))
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
        @RequestParam("created_at", required = false) createdAt: String? = null,
        @RequestParam("updated_at", required = false) updatedAt: String? = null,
        @RequestParam("sort", required = false) sort: String? = null,
        @RequestParam("page", required = false) page: Int? = null,
        @RequestParam("per_page", required = false) perPage: Int? = null,
        @RequestParam("fields", required = false) fields: Collection<String>? = null,
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
            perPage = perPage,
            page = page
        )

        return offsetPage.mapDataAsync { mapperContext.map(Projection(it, projectionParser.parse(fields))) }
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @GetMapping("/self")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'clients[self]:read')")
    suspend fun readSelf(
        @AuthenticationPrincipal principal: ClientEntity,
        @RequestParam("fields", required = false) fields: Collection<String>? = null,
    ): ClientInfo {
        return read(principal.clientId ?: throw EmptyResultDataAccessException(1), fields)
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @GetMapping("/{client-id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission({null, #clientId}, {'clients:read', 'clients[self]:read'})")
    suspend fun read(
        @PathVariable("client-id") clientId: ULID,
        @RequestParam("fields", required = false) fields: Collection<String>? = null,
    ): ClientInfo {
        val client = clientStorage.loadOrFail(clientId)
        return mapperContext.map(Projection(client, projectionParser.parse(fields)))
    }

    @Operation(security = [SecurityRequirement(name = "bearer")])
    @PatchMapping("/{client-id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission({null, #clientId}, {'clients:update', 'clients[self]:update'})")
    suspend fun update(
        @PathVariable("client-id") clientId: ULID,
        @Valid @RequestBody request: UpdateClientRequest,
        @RequestParam("fields", required = false) fields: Collection<String>? = null,
    ): ClientInfo = operator.executeAndAwait {
        val client = clientStorage.loadOrFail(clientId)

        request.scope?.let {
            if (it.isPresent) {
                syncScope(clientId, it.get())
            } else {
                val existsScope = client.getScope(deep = false).toSet()
                existsScope.forEach { client.revoke(it) }
            }
        }

        val patch = PropertyOverridePatch.of<Client, UpdateClientRequest>(request.copy(scope = null))
        patch.apply(client).sync()

        mapperContext.map(Projection(client, projectionParser.parse(fields)))
    }!!

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

    private suspend fun syncScope(
        clientId: ULID,
        scope: Collection<ULID>
    ) = operator.executeAndAwait {
        val client = clientStorage.loadOrFail(clientId)

        val existsScope = client.getScope(deep = false).toSet()
        val requestScope = scopeTokenStorage.load(scope).toSet()

        val toGrantScope = requestScope.filter { !existsScope.contains(it) }
        val toRevokeScope = existsScope.filter { !requestScope.contains(it) }

        toGrantScope.forEach { grant(clientId, it.id) }
        toRevokeScope.forEach { revoke(clientId, it.id) }
    }

    private suspend fun grant(
        clientId: ULID,
        scopeId: ULID
    ) = authorizator.withAuthorize(listOf("clients.scope:create"), null) {
        val client = clientStorage.loadOrFail(clientId)
        val scopeToken = scopeTokenStorage.loadOrFail(scopeId)
        client.grant(scopeToken)
    }

    private suspend fun revoke(
        clientId: ULID,
        scopeId: ULID
    ) = authorizator.withAuthorize(listOf("clients.scope:delete"), null) {
        val client = clientStorage.loadOrFail(clientId)
        val scopeToken = scopeTokenStorage.loadOrFail(scopeId)
        client.revoke(scopeToken)
    }
}
