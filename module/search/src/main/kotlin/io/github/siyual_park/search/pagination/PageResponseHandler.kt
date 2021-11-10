package io.github.siyual_park.search.pagination

import io.github.siyual_park.view.ResponseBodyResultHandlerAdapter
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.reactive.accept.RequestedContentTypeResolver

@ControllerAdvice
class PageResponseHandler(
    serverCodecConfigurer: ServerCodecConfigurer,
    resolver: RequestedContentTypeResolver,
    presenter: OffsetPagePresenter
) : ResponseBodyResultHandlerAdapter<OffsetPage<*>>(
    serverCodecConfigurer.writers,
    resolver,
    presenter
)
