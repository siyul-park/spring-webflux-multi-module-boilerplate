package io.github.siyual_park.mapper

data class MappingInfo<SOURCE, TARGET>(
    val source: Class<SOURCE>,
    val target: Class<TARGET>
)
