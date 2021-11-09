package io.github.siyual_park.mapper

import java.lang.reflect.Type

data class MappingInfo(
    val source: Type,
    val target: Type
)
