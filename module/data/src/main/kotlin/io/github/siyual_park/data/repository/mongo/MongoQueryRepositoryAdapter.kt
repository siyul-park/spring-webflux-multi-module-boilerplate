package io.github.siyual_park.data.repository.mongo

import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.patch.SuspendPatch
import io.github.siyual_park.data.repository.QueryRepository
import io.github.siyual_park.data.repository.Repository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.springframework.data.domain.Sort
import kotlin.reflect.KClass

class MongoQueryRepositoryAdapter<T : Any, ID : Any>(
    private val delegator: MongoRepository<T, ID>,
    clazz: KClass<T>
) : QueryRepository<T, ID>, Repository<T, ID> by delegator {
    private val parser = MongoCriteriaParser(clazz)

    override suspend fun exists(criteria: Criteria): Boolean {
        return parser.parse(criteria)?.let { delegator.exists(it) } ?: false
    }

    override suspend fun findOne(criteria: Criteria): T? {
        return parser.parse(criteria)?.let { delegator.findOne(it) }
    }

    override fun findAll(criteria: Criteria?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return delegator.findAll(criteria?.let { parser.parse(it) }, limit, offset, sort)
    }

    override suspend fun update(criteria: Criteria, patch: Patch<T>): T? {
        return parser.parse(criteria)?.let { delegator.update(it, patch) }
    }

    override suspend fun update(criteria: Criteria, patch: SuspendPatch<T>): T? {
        return parser.parse(criteria)?.let { delegator.update(it, patch) }
    }

    override fun updateAll(criteria: Criteria, patch: Patch<T>, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return parser.parse(criteria)?.let { delegator.updateAll(it, patch, limit, offset, sort) } ?: emptyFlow()
    }

    override fun updateAll(
        criteria: Criteria,
        patch: SuspendPatch<T>,
        limit: Int?,
        offset: Long?,
        sort: Sort?
    ): Flow<T> {
        return parser.parse(criteria)?.let { delegator.updateAll(it, patch, limit, offset, sort) } ?: emptyFlow()
    }

    override suspend fun count(criteria: Criteria?, limit: Int?): Long {
        return delegator.count(criteria?.let { parser.parse(it) }, limit)
    }

    override suspend fun deleteAll(criteria: Criteria?, limit: Int?, offset: Long?, sort: Sort?) {
        return delegator.deleteAll(criteria?.let { parser.parse(it) }, limit, offset, sort)
    }
}
