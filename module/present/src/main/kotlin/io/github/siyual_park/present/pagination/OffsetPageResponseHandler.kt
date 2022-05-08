package io.github.siyual_park.present.pagination

import io.github.siyual_park.present.ResponseBodyResultHandlerAdapter
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.reactive.accept.RequestedContentTypeResolver

@ControllerAdvice
class OffsetPageResponseHandler(
    serverCodecConfigurer: ServerCodecConfigurer,
    resolver: RequestedContentTypeResolver,
    presenter: OffsetPagePresenter
) : ResponseBodyResultHandlerAdapter<OffsetPage<*>>(
    serverCodecConfigurer.writers,
    resolver,
    presenter
)
