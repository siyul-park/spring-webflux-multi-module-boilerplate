package io.github.siyual_park.data.criteria

import kotlin.reflect.KClass

@Suppress("UNCHECKED_CAST")
class CriteriaParser<OUT : Any?> {
    private val strategies = mutableMapOf<KClass<*>, CriteriaParseStrategy<*, OUT>>()

    fun <IN : Criteria> register(clazz: KClass<IN>, strategy: CriteriaParseStrategy<IN, OUT>) {
        strategies[clazz] = strategy
    }

    fun <IN : Criteria> parse(criteria: IN): OUT {
        val clazz = criteria.javaClass.kotlin
        val strategy = strategies[clazz] ?: throw RuntimeException()
        strategy as CriteriaParseStrategy<IN, OUT>

        return strategy.parse(criteria)
    }
}
