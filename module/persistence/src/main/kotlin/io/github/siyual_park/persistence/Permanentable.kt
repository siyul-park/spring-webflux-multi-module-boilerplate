package io.github.siyual_park.persistence

interface Permanentable {
    suspend fun link(): Boolean
    suspend fun clear()
    suspend fun sync(): Boolean
}
