plugins {
    application

    id("org.springframework.boot")
    id("io.spring.dependency-management")

    kotlin("plugin.spring")
}

dependencies {
    api("org.springframework.boot:spring-boot-starter-webflux")
    api("org.springframework.boot:spring-boot-starter-security")

    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}
