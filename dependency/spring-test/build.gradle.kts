plugins {
    application

    id("org.springframework.boot")
    id("io.spring.dependency-management")

    kotlin("plugin.spring")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-test")
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}
