package io.github.siyual_park.application.server.controller

import io.github.siyual_park.application.server.dto.response.CacheStatusInfo
import io.github.siyual_park.auth.domain.authorization.Authorizator
import io.github.siyual_park.auth.domain.authorization.authorize
import io.github.siyual_park.data.cache.StorageManager
import io.github.siyual_park.mapper.MapperContext
import io.github.siyual_park.mapper.map
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.security.SecurityRequirement
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.http.HttpStatus
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.ResponseStatus
import org.springframework.web.bind.annotation.RestController

@Tag(name = "cache")
@RestController
@RequestMapping("/cache")
class CacheController(
    private val cacheStorageManager: StorageManager,
    private val authorizator: Authorizator,
    private val mapperContext: MapperContext
) {

    @Operation(security = [SecurityRequirement(name = "Bearer")])
    @GetMapping("/status")
    @ResponseStatus(HttpStatus.OK)
    suspend fun status(): Map<String, CacheStatusInfo> = authorizator.authorize(listOf("cache.status:read")) {
        mapperContext.map(cacheStorageManager.status())
    }
}
