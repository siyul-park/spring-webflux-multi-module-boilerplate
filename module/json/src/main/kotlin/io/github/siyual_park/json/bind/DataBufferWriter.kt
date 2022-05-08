package io.github.siyual_park.json.bind

import com.fasterxml.jackson.databind.ObjectMapper
import org.slf4j.LoggerFactory
import org.springframework.core.io.buffer.DataBufferFactory
import org.springframework.http.MediaType
import org.springframework.http.server.reactive.ServerHttpResponse
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component
class DataBufferWriter(
    private val objectMapper: ObjectMapper
) {
    private val logger = LoggerFactory.getLogger(DataBufferWriter::class.java)

    fun <T> write(httpResponse: ServerHttpResponse, obj: T): Mono<Void> {
        return httpResponse
            .writeWith(
                Mono.fromSupplier {
                    httpResponse.headers.contentType = MediaType.APPLICATION_JSON

                    val bufferFactory: DataBufferFactory = httpResponse.bufferFactory()
                    try {
                        bufferFactory.wrap(objectMapper.writeValueAsBytes(obj))
                    } catch (ex: Exception) {
                        logger.warn("Error writing response", ex)
                        bufferFactory.wrap(ByteArray(0))
                    }
                }
            )
    }
}
