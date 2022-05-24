package io.github.siyual_park.data.criteria

interface CriteriaParser<T : Any, Out : Any> {
    fun parse(criteria: Criteria<T>): Out
}
