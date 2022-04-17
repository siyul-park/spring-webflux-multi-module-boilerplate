package io.github.siyual_park.data.test

import com.mongodb.reactivestreams.client.MongoClient
import com.mongodb.reactivestreams.client.MongoClients
import de.flapdoodle.embed.mongo.MongodStarter
import de.flapdoodle.embed.mongo.config.MongodConfig
import de.flapdoodle.embed.mongo.config.Net
import de.flapdoodle.embed.mongo.distribution.Version
import de.flapdoodle.embed.process.runtime.Network

fun createMemMongoClients(): MongoClient {
    val starter = MongodStarter.getDefaultInstance()

    val port = Network.getFreeServerPort()
    val mongodConfig = MongodConfig.builder()
        .version(Version.Main.PRODUCTION)
        .net(Net(port, Network.localhostIsIPv6()))
        .build()

    val mongodExecutable = starter.prepare(mongodConfig)
    mongodExecutable.start()

    return MongoClients.create("mongodb://localhost:$port")!!
}
