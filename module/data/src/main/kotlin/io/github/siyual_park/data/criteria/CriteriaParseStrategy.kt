package io.github.siyual_park.data.criteria

interface CriteriaParseStrategy<IN : Criteria, OUT : Any?> {
    fun parse(criteria: IN): OUT
}
