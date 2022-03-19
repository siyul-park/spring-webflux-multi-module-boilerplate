package io.github.siyual_park.application.server.dto.request

import java.net.URL

data class MutableClientData(
    var name: String,
    var origin: URL,
)
