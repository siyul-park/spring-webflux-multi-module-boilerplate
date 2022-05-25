package io.github.siyual_park.data.criteria

interface CriteriaParser<Out : Any?> {
    fun parse(criteria: Criteria): Out
}
