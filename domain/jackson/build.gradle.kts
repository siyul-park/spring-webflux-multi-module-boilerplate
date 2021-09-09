val jackson_version: String by project

plugins {
    application

    id("org.springframework.boot")
    id("io.spring.dependency-management")

    kotlin("plugin.spring")
}

dependencies {
    implementation("org.springframework.boot:spring-boot-starter-webflux")
    testImplementation("org.springframework.boot:spring-boot-starter-test")

    api("com.fasterxml.jackson.core:jackson-core:$jackson_version")
    api("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
