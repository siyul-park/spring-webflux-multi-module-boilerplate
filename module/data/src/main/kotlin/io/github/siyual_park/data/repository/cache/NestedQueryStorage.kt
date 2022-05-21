package io.github.siyual_park.data.repository.cache

interface NestedQueryStorage<T : Any> : QueryStorage<T>, GeneralNestedStorage<NestedQueryStorage<T>>
