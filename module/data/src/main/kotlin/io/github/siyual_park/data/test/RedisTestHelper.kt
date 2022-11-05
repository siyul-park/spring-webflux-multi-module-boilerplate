package io.github.siyual_park.data.test

import de.flapdoodle.embed.process.runtime.Network
import org.redisson.Redisson
import org.redisson.api.RedissonClient
import org.redisson.config.Config
import redis.embedded.RedisServer
import redis.embedded.exceptions.EmbeddedRedisException

class RedisTestHelper : ResourceTestHelper {
    private val port = Network.freeServerPort(Network.getLocalHost())
    private val redisServer = RedisServer(port).also {
        it.start()
    }

    private val config = Config().apply {
        useSingleServer().address = "redis://localhost:$port"
    }

    val redisClient: RedissonClient = Redisson.create(config)

    override fun setUp() {
        try {
            redisServer.start()
        } catch (_: EmbeddedRedisException) {
        }
    }

    override fun tearDown() {
        redisClient.shutdown()
        redisServer.stop()
    }
}
