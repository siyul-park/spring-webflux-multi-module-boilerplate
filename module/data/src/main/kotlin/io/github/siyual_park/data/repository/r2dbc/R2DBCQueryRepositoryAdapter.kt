package io.github.siyual_park.data.repository.r2dbc

import io.github.siyual_park.data.criteria.Criteria
import io.github.siyual_park.data.patch.Patch
import io.github.siyual_park.data.patch.SuspendPatch
import io.github.siyual_park.data.repository.QueryRepository
import io.github.siyual_park.data.repository.Repository
import kotlinx.coroutines.flow.Flow
import org.springframework.data.domain.Sort

class R2DBCQueryRepositoryAdapter<T : Any, ID : Any>(
    private val delegator: R2DBCRepository<T, ID>
) : QueryRepository<T, ID>, Repository<T, ID> by delegator {
    private val parser = R2DBCCriteriaParser<T>()

    override suspend fun exists(criteria: Criteria<T>): Boolean {
        return delegator.exists(parser.parse(criteria))
    }

    override suspend fun findOne(criteria: Criteria<T>): T? {
        return delegator.findOne(parser.parse(criteria))
    }

    override fun findAll(criteria: Criteria<T>?, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return delegator.findAll(criteria?.let { parser.parse(it) }, limit, offset, sort)
    }

    override suspend fun update(criteria: Criteria<T>, patch: Patch<T>): T? {
        return delegator.update(parser.parse(criteria), patch)
    }

    override suspend fun update(criteria: Criteria<T>, patch: SuspendPatch<T>): T? {
        return delegator.update(parser.parse(criteria), patch)
    }

    override fun updateAll(criteria: Criteria<T>, patch: Patch<T>, limit: Int?, offset: Long?, sort: Sort?): Flow<T> {
        return delegator.updateAll(parser.parse(criteria), patch, limit, offset, sort)
    }

    override fun updateAll(
        criteria: Criteria<T>,
        patch: SuspendPatch<T>,
        limit: Int?,
        offset: Long?,
        sort: Sort?
    ): Flow<T> {
        return delegator.updateAll(parser.parse(criteria), patch, limit, offset, sort)
    }

    override suspend fun count(criteria: Criteria<T>?, limit: Int?): Long {
        return delegator.count(criteria?.let { parser.parse(it) }, limit)
    }

    override suspend fun deleteAll(criteria: Criteria<T>?, limit: Int?, offset: Long?, sort: Sort?) {
        return delegator.deleteAll(criteria?.let { parser.parse(it) }, limit, offset, sort)
    }
}
