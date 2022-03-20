package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.request.CreateClientRequest
import io.github.siyual_park.application.server.dto.request.MutableClientData
import io.github.siyual_park.application.server.dto.response.ClientDetailInfo
import io.github.siyual_park.application.server.dto.response.ClientInfo
import io.github.siyual_park.client.domain.ClientFactory
import io.github.siyual_park.client.domain.ClientFinder
import io.github.siyual_park.client.domain.ClientPaginatorFactory
import io.github.siyual_park.client.domain.ClientRemover
import io.github.siyual_park.client.domain.ClientUpdater
import io.github.siyual_park.client.domain.CreateClientPayload
import io.github.siyual_park.client.entity.Client
import io.github.siyual_park.client.entity.ClientEntity
import io.github.siyual_park.json.patch.JsonMergePatch
import io.github.siyual_park.json.patch.PatchConverter
import io.github.siyual_park.json.patch.convert
import io.github.siyual_park.mapper.MapperManager
import io.github.siyual_park.mapper.map
import io.github.siyual_park.reader.filter.RHSFilterParserFactory
import io.github.siyual_park.reader.finder.findByIdOrFail
import io.github.siyual_park.reader.pagination.OffsetPage
import io.github.siyual_park.reader.pagination.OffsetPageQuery
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
    private val clientRemover: ClientRemover,
    private val clientFinder: ClientFinder,
    private val clientUpdater: ClientUpdater,
    private val clientPaginatorFactory: ClientPaginatorFactory,
    private val patchConverter: PatchConverter,
    rhsFilterParserFactory: RHSFilterParserFactory,
    sortParserFactory: SortParserFactory,
    private val mapperManager: MapperManager
) {
    private val rhsFilterParser = rhsFilterParserFactory.create(Client::class)
    private val sortParser = sortParserFactory.create(Client::class)

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
                Client::id to listOf(id),
                Client::name to listOf(name),
                Client::type to listOf(type),
                Client::origin to listOf(origin),
                Client::createdAt to listOf(createdAt),
                Client::updatedAt to listOf(updatedAt)
            )
        )
        val paginator = clientPaginatorFactory.create(
            criteria = criteria,
            sort = sort?.let { sortParser.parse(it) }
        )
        val offsetPage = paginator.paginate(
            OffsetPageQuery(
                page = page ?: 0,
                perPage = perPage ?: 15,
            )
        )
        return offsetPage.mapDataAsync { mapperManager.map(it) }
    }

    @GetMapping("/self")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'clients[self]:read')")
    suspend fun readSelf(@AuthenticationPrincipal principal: ClientEntity): ClientInfo {
        val client = principal.clientId?.let { clientFinder.findById(it) }
            ?: throw EmptyResultDataAccessException(1)
        return mapperManager.map(client)
    }

    @PatchMapping("/self")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission(null, 'clients[self]:update')")
    suspend fun updateSelf(
        @AuthenticationPrincipal principal: ClientEntity,
        @Valid @RequestBody patch: JsonMergePatch<MutableClientData>
    ): ClientInfo {
        return clientUpdater.updateById(
            principal.clientId ?: throw EmptyResultDataAccessException(1),
            patchConverter.convert(patch)
        )
            .let { mapperManager.map(it) }
    }

    @DeleteMapping("/self")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission(null, 'clients[self]:delete')")
    suspend fun deleteSelf(@AuthenticationPrincipal principal: ClientEntity) {
        clientRemover.remove(principal.clientId ?: throw EmptyResultDataAccessException(1), soft = true)
    }

    @GetMapping("/{client-id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission({null, #clientId}, {'clients:read', 'clients[self]:read'})")
    suspend fun read(@PathVariable("client-id") clientId: Long): ClientInfo {
        val client = clientFinder.findByIdOrFail(clientId)
        return mapperManager.map(client)
    }

    @PatchMapping("/{client-id}")
    @ResponseStatus(HttpStatus.OK)
    @PreAuthorize("hasPermission({null, #clientId}, {'clients:update', 'clients[self]:update'})")
    suspend fun update(
        @PathVariable("client-id") clientId: Long,
        @Valid @RequestBody patch: JsonMergePatch<MutableClientData>
    ): ClientInfo {
        return clientUpdater.updateById(
            clientId,
            patchConverter.convert(patch)
        )
            .let { mapperManager.map(it) }
    }

    @DeleteMapping("/{client-id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasPermission({null, #clientId}, {'clients:delete', 'clients[self]:delete'})")
    suspend fun delete(
        @PathVariable("client-id") clientId: Long,
    ) {
        clientRemover.remove(clientId, soft = true)
    }
}
