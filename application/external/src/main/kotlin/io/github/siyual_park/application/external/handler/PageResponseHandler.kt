package io.github.siyual_park.application.external.handler

import io.github.siyual_park.search.pagination.OffsetPage
import io.github.siyual_park.search.pagination.OffsetPagePresenter
import io.github.siyual_park.search.pagination.ResponseBodyResultHandlerAdapter
import org.springframework.http.codec.ServerCodecConfigurer
import org.springframework.web.bind.annotation.ControllerAdvice
import org.springframework.web.reactive.accept.RequestedContentTypeResolver

@ControllerAdvice
class PageResponseHandler(
    serverCodecConfigurer: ServerCodecConfigurer,
    resolver: RequestedContentTypeResolver,
    presenter: OffsetPagePresenter
) : ResponseBodyResultHandlerAdapter<OffsetPage<*>>(serverCodecConfigurer.writers, resolver, presenter)
