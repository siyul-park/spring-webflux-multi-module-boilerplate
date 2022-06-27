package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.request.CreateClientRequest
import io.github.siyual_park.application.server.dto.request.UpdateClientRequest
import io.github.siyual_park.application.server.dto.response.ClientDetailInfo
import io.github.siyual_park.application.server.dto.response.ClientInfo
import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.authorization.withAuthorize
import io.github.siyual_park.auth.domain.scope_token.ScopeToken
import io.github.siyual_park.auth.domain.scope_token.ScopeTokenStorage
import io.github.siyual_park.client.domain.Client
import io.github.siyual_park.client.domain.ClientFactory
import io.github.siyual_park.client.domain.ClientStorage
import io.github.siyual_park.client.domain.CreateClientPayload
import io.github.siyual_park.client.entity.ClientData
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
    private val rhsFilterParser = rhsFilterParserFactory.create(ClientData::class)
    private val sortParser = sortParserFactory.create(ClientData::class)
    private val projectionParser = projectionParserFactory.create(ClientInfo::class)

    private val offsetPaginator = OffsetPaginator(clientStorage)

    @Operation(security = [SecurityRequirement(name = "Bearer")])
    @PostMapping("")
    @ResponseStatus(HttpStatus.CREATED)
    suspend fun create(
        @Valid @RequestBody request: CreateClientRequest,
        @RequestParam("fields", required = false) fields: Collection<String>? = null,
    ): ClientDetailInfo = authorizator.withAuthorize(listOf("clients:create")) {
        val projectionNode = projectionParser.parse(fields)
        val payload = CreateClientPayload(
            name = request.name,
            type = request.type,
            origin = request.origin
        )
        val client = clientFactory.create(payload)
        mapperContext.map(Projection(client, projectionNode))
    }

    @Operation(security = [SecurityRequirement(name = "Bearer")])
    @GetMapping("")
    @ResponseStatus(HttpStatus.OK)
    suspend fun readAll(
        @RequestParam("id", required = false) id: List<String>? = null,
        @RequestParam("name", required = false) name: List<String>? = null,
        @RequestParam("type", required = false) type: List<String>? = null,
        @RequestParam("origin", required = false) origin: List<String>? = null,
        @RequestParam("created_at", required = false) createdAt: List<String>? = null,
        @RequestParam("updated_at", required = false) updatedAt: List<String>? = null,
        @RequestParam("sort", required = false) sort: List<String>? = null,
        @RequestParam("page", required = false) page: Int? = null,
        @RequestParam("per_page", required = false) perPage: Int? = null,
        @RequestParam("fields", required = false) fields: Collection<String>? = null,
    ): OffsetPage<ClientInfo> = authorizator.withAuthorize(listOf("clients:read")) {
        val projectionNode = projectionParser.parse(fields)
        val criteria = rhsFilterParser.parse(
            mapOf(
                ClientData::id to id,
                ClientData::name to name,
                ClientData::type to type,
                ClientData::origin to origin,
                ClientData::createdAt to createdAt,
                ClientData::updatedAt to updatedAt
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
    @GetMapping("/{client-id}")
    @ResponseStatus(HttpStatus.OK)
    suspend fun read(
        @PathVariable("client-id") clientId: ULID,
        @RequestParam("fields", required = false) fields: Collection<String>? = null,
    ): ClientInfo = authorizator.withAuthorize(listOf("clients:read", "clients[self]:read"), listOf(null, clientId)) {
        val projectionNode = projectionParser.parse(fields)
        val client = clientStorage.loadOrFail(clientId)
        mapperContext.map(Projection(client, projectionNode))
    }

    @Operation(security = [SecurityRequirement(name = "Bearer")])
    @PatchMapping("/{client-id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission({null, #clientId}, {'clients:update', 'clients[self]:update'})")
    suspend fun update(
        @PathVariable("client-id") clientId: ULID,
        @Valid @RequestBody request: UpdateClientRequest,
        @RequestParam("fields", required = false) fields: Collection<String>? = null,
    ): ClientInfo = authorizator.withAuthorize(listOf("clients:update", "clients[self]:update"), listOf(null, clientId)) {
        operator.executeAndAwait {
            val projectionNode = projectionParser.parse(fields)
            val client = clientStorage.loadOrFail(clientId)

            request.scope?.let {
                if (it.isPresent) {
                    syncScope(client, it.get().let { scopeTokenStorage.load(it) }.toSet())
                } else {
                    val existsScope = client.getScope(deep = false).toSet()
                    existsScope.forEach { client.revoke(it) }
                }
            }

            val patch = PropertyOverridePatch.of<Client, UpdateClientRequest>(request.copy(scope = null))
            patch.apply(client).sync()

            mapperContext.map(Projection(client, projectionNode))
        }!!
    }

    @Operation(security = [SecurityRequirement(name = "Bearer")])
    @DeleteMapping("/{client-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    suspend fun delete(
        @PathVariable("client-id") clientId: ULID,
    ) = authorizator.withAuthorize(listOf("clients:delete", "clients[self]:delete"), listOf(null, clientId)) {
        val client = clientStorage.loadOrFail(clientId)
        client.clear()
    }

    private suspend fun syncScope(
        client: Client,
        scope: Set<ScopeToken>
    ) = operator.executeAndAwait {
        val existsScope = client.getScope(deep = false).toSet()

        val toGrantScope = scope.filter { !existsScope.contains(it) }
        val toRevokeScope = existsScope.filter { !scope.contains(it) }

        if (toGrantScope.isNotEmpty()) {
            authorizator.withAuthorize(listOf("clients.scope:create"), null) {
                toGrantScope.forEach { client.grant(it) }
            }
        }
        if (toRevokeScope.isNotEmpty()) {
            authorizator.withAuthorize(listOf("clients.scope:delete"), null) {
                toRevokeScope.forEach { client.revoke(it) }
            }
        }
    }
}
