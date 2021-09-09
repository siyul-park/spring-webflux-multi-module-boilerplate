package io.github.siyual_park

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.context.properties.ConfigurationPropertiesScan
import org.springframework.boot.runApplication
import org.springframework.cache.annotation.EnableCaching

@EnableCaching
@SpringBootApplication
@ConfigurationPropertiesScan
class AppInterfaceApplication

fun main(args: Array<String>) {
    runApplication<AppInterfaceApplication>(*args)
}
