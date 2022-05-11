package io.github.siyual_park.data.repository.in_memory

import io.github.siyual_park.data.repository.Repository

interface InMemoryRepository<T : Any, ID : Any> : Repository<T, ID>
