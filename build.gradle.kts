val kotlin_version: String by project
val junit_version: String by project
val jacoco_version: String by project
val coroutines_version: String by project
val projectreactor_version: String by project
val reactor_kotlin_extensions_version: String by project
val guava_version: String by project
val jackson_version: String by project
val json_patch_version: String by project
val springdoc_version: String by project
val sentry_logback_version: String by project
val embed_mongo_version: String by project

buildscript {
    val kotlin_version: String by project
    val ktlint_version: String by project

    repositories {
        maven { url = uri("https://plugins.gradle.org/m2/") }
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-allopen:$kotlin_version")
        classpath("org.jetbrains.kotlin:kotlin-noarg:$kotlin_version")
        classpath("org.jlleitschuh.gradle:ktlint-gradle:$ktlint_version")
    }
}

plugins {
    application

    kotlin("jvm")
    id("org.jlleitschuh.gradle.ktlint")
    id("jacoco")

    id("org.springframework.boot")
    id("io.spring.dependency-management")
    kotlin("plugin.spring")
}

jacoco {
    toolVersion = jacoco_version
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}

group = "io.github.siyual-park"
version = "0.0.0-SNAPSHOT"

allprojects {
    apply(plugin = "kotlin")
    apply(plugin = "org.jlleitschuh.gradle.ktlint")
    apply(plugin = "jacoco")

    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")

    repositories {
        mavenLocal()
        mavenCentral()
    }

    dependencies {
        implementation(kotlin("stdlib"))

        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutines_version")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$coroutines_version")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutines_version")

        testImplementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit_version")

        implementation("io.sentry:sentry-logback:$sentry_logback_version")

        implementation("org.springframework.boot:spring-boot-starter-webflux")
        implementation("org.springframework.boot:spring-boot-starter-security")
        implementation("org.springframework.boot:spring-boot-starter-validation")

        annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")
        testImplementation("org.springframework.boot:spring-boot-starter-test")

        implementation("io.projectreactor.kotlin:reactor-kotlin-extensions:$reactor_kotlin_extensions_version")

        implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
        implementation("org.springframework.boot:spring-boot-starter-data-mongodb-reactive")

        implementation("io.r2dbc:r2dbc-h2")
        implementation("io.r2dbc:r2dbc-pool")
        implementation("io.r2dbc:r2dbc-postgresql")

        runtimeOnly("com.h2database:h2")
        implementation("de.flapdoodle.embed:de.flapdoodle.embed.mongo:$embed_mongo_version")

        testImplementation("io.projectreactor:reactor-test:$projectreactor_version")
        testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_version")

        implementation("com.google.guava:guava:$guava_version")

        implementation("com.fasterxml.jackson.core:jackson-core:$jackson_version")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")

        implementation("com.github.java-json-tools:json-patch:$json_patch_version")

        implementation("org.springdoc:springdoc-openapi-ui:$springdoc_version")
        implementation("org.springdoc:springdoc-openapi-webflux-ui:$springdoc_version")
        implementation("org.springdoc:springdoc-openapi-security:$springdoc_version")
        implementation("org.springdoc:springdoc-openapi-kotlin:$springdoc_version")
    }

    tasks.withType<Copy> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
        sourceCompatibility = JavaVersion.VERSION_11.toString()
        targetCompatibility = JavaVersion.VERSION_11.toString()

        kotlinOptions {
            freeCompilerArgs = listOf(
                "-Xjsr305=strict",
                "-Xemit-jvm-type-annotations"
            )
            jvmTarget = JavaVersion.VERSION_11.toString()
        }
    }
    tasks.withType<Test> {
        useJUnitPlatform()
    }
}
