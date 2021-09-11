val junit_version: String by project

plugins {
    application

    id("org.springframework.boot")
    id("io.spring.dependency-management")

    kotlin("plugin.spring")
}

dependencies {
    api(project(":dependency:async-test"))

    implementation(project(":module:data"))

    implementation(project(":dependency:spring"))
    testImplementation(project(":dependency:spring-test"))

    implementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
    runtimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit_version")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}
