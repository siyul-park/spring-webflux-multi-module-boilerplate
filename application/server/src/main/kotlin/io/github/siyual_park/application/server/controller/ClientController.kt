package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.request.CreateClientRequest
import io.github.siyual_park.application.server.dto.request.UpdateClientRequest
import io.github.siyual_park.application.server.dto.response.ClientDetailInfo
import io.github.siyual_park.application.server.dto.response.ClientInfo
import io.github.siyual_park.client.domain.ClientFactory
import io.github.siyual_park.client.domain.ClientStorage
import io.github.siyual_park.client.domain.CreateClientPayload
import io.github.siyual_park.client.entity.ClientData
import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.mapper.MapperManager
import io.github.siyual_park.mapper.map
import io.github.siyual_park.persistence.loadOrFail
import io.github.siyual_park.reader.filter.RHSFilterParserFactory
import io.github.siyual_park.reader.pagination.OffsetPage
import io.github.siyual_park.reader.pagination.OffsetPaginator
import io.github.siyual_park.reader.sort.SortParserFactory
import io.swagger.annotations.Api
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

@Api(tags = ["client"])
@RestController
@RequestMapping("/clients")
class ClientController(
    private val clientFactory: ClientFactory,
    private val clientStorage: ClientStorage,
    rhsFilterParserFactory: RHSFilterParserFactory,
    sortParserFactory: SortParserFactory,
    private val mapperManager: MapperManager
) {
    private val rhsFilterParser = rhsFilterParserFactory.create(ClientData::class)
    private val sortParser = sortParserFactory.create(ClientData::class)

    private val offsetPaginator = OffsetPaginator(clientStorage)

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
        return mapperManager.map(client)
    }

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
        val criteria = rhsFilterParser.parseFromProperty(
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

        return offsetPage.mapDataAsync { mapperManager.map(it) }
    }

    @GetMapping("/self")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'clients[self]:read')")
    suspend fun readSelf(@AuthenticationPrincipal principal: ClientEntity): ClientInfo {
        val clientId = principal.clientId ?: throw EmptyResultDataAccessException(1)
        val client = clientStorage.loadOrFail(clientId)
        return mapperManager.map(client)
    }

    @PatchMapping("/self")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'clients[self]:update')")
    suspend fun updateSelf(
        @AuthenticationPrincipal principal: ClientEntity,
        @Valid @RequestBody request: UpdateClientRequest
    ): ClientInfo {
        val clientId = principal.clientId ?: throw EmptyResultDataAccessException(1)
        val client = clientStorage.loadOrFail(clientId)

        request.name?.ifPresent { client.name = it }
        request.origin?.ifPresent { client.origin = it }

        client.sync()
        return mapperManager.map(client)
    }

    @DeleteMapping("/self")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'clients[self]:delete')")
    suspend fun deleteSelf(@AuthenticationPrincipal principal: ClientEntity) {
        val clientId = principal.clientId ?: throw EmptyResultDataAccessException(1)
        val client = clientStorage.loadOrFail(clientId)
        client.clear()
    }

    @GetMapping("/{client-id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission({null, #clientId}, {'clients:read', 'clients[self]:read'})")
    suspend fun read(@PathVariable("client-id") clientId: Long): ClientInfo {
        val client = clientStorage.loadOrFail(clientId)
        return mapperManager.map(client)
    }

    @PatchMapping("/{client-id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission({null, #clientId}, {'clients:update', 'clients[self]:update'})")
    suspend fun update(
        @PathVariable("client-id") clientId: Long,
        @Valid @RequestBody request: UpdateClientRequest
    ): ClientInfo {
        val client = clientStorage.loadOrFail(clientId)

        request.name?.ifPresent { client.name = it }
        request.origin?.ifPresent { client.origin = it }

        client.sync()

        return mapperManager.map(client)
    }

    @DeleteMapping("/{client-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission({null, #clientId}, {'clients:delete', 'clients[self]:delete'})")
    suspend fun delete(
        @PathVariable("client-id") clientId: Long,
    ) {
        val client = clientStorage.loadOrFail(clientId)
        client.clear()
    }
}
