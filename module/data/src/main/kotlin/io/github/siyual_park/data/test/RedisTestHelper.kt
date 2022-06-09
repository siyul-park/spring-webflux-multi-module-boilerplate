package io.github.siyual_park.data.test

import de.flapdoodle.embed.process.runtime.Network
import org.redisson.Redisson
import org.redisson.config.Config
import redis.embedded.RedisServer
import redis.embedded.exceptions.EmbeddedRedisException

class RedisTestHelper : ResourceTestHelper {
    private val port = Network.getFreeServerPort()
    private val redisServer = RedisServer(port).also {
        it.start()
    }

    private val config = Config().apply {
        useSingleServer().address = "redis://localhost:$port"
    }

    val redisClient = Redisson.create(config)

    override fun setUp() {
        try {
            redisServer.start()
        } catch (_: EmbeddedRedisException) {
        }
    }

    override fun tearDown() {
        redisServer.stop()
    }
}
