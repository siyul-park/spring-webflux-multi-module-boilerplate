plugins {
    application

    id("org.springframework.boot")
    id("io.spring.dependency-management")

    kotlin("plugin.spring")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-data-r2dbc")
    api("io.r2dbc:r2dbc-h2")

    runtimeOnly("com.h2database:h2")
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}
