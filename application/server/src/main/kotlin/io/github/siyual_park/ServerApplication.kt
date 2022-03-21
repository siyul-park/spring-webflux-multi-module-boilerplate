package io.github.siyual_park

import io.github.siyual_park.application.server.been.FullBeanNameGenerator
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching
import org.springframework.scheduling.annotation.EnableScheduling

@EnableCaching
@EnableScheduling
@SpringBootApplication(nameGenerator = FullBeanNameGenerator::class)
@ConfigurationPropertiesScan
class ServerApplication

fun main(args: Array<String>) {
    runApplication<ServerApplication>(*args)
}
