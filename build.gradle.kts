val kotlin_version: String by project
val junit_version: String by project
val mockk_version: String by project
val jacoco_version: String by project
val coroutines_version: String by project
val projectreactor_version: String by project
val reactor_kotlin_extensions_version: String by project
val guava_version: String by project
val apache_commons_collections_version: String by project
val jackson_version: String by project
val json_patch_version: String by project
val springdoc_version: String by project
val sentry_logback_version: String by project
val embed_mongo_version: String by project
val javafaker_version: String by project
val redisson_version: String by project
val embedded_redis_version: String by project

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
        implementation(kotlin("reflect"))

        implementation("org.jetbrains.kotlin:kotlin-reflect")
        implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")

        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutines_version")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutines_version")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactive:$coroutines_version")
        implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$coroutines_version")

        testImplementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
        testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit_version")
        testImplementation("io.mockk:mockk:$mockk_version")

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
        implementation("org.apache.commons:commons-collections4:$apache_commons_collections_version")

        implementation("com.fasterxml.jackson.core:jackson-core:$jackson_version")
        implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$jackson_version")

        implementation("com.github.java-json-tools:json-patch:$json_patch_version")

        implementation("org.springdoc:springdoc-openapi-ui:$springdoc_version")
        implementation("org.springdoc:springdoc-openapi-webflux-ui:$springdoc_version")
        implementation("org.springdoc:springdoc-openapi-security:$springdoc_version")
        implementation("org.springdoc:springdoc-openapi-kotlin:$springdoc_version")

        implementation("com.github.javafaker:javafaker:$javafaker_version")

        implementation("org.redisson:redisson-spring-boot-starter:$redisson_version")
        implementation("it.ozimov:embedded-redis:$embedded_redis_version") {
            exclude("org.slf4j")
            exclude("ch.qos.logback")
        }
    }

    java {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    tasks.withType<Copy> {
        duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    }
    tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile>().all {
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

// task to gather code coverage from multiple subprojects
// NOTE: the `JacocoReport` tasks do *not* depend on the `test` task by default. Meaning you have to ensure
// that `test` (or other tasks generating code coverage) run before generating the report.
// You can achieve this by calling the `test` lifecycle task manually
// $ ./gradlew test codeCoverageReport
tasks.register<JacocoReport>("codeCoverageReport") {
    // If a subproject applies the 'jacoco' plugin, add the result it to the report
    subprojects {
        val subproject = this
        subproject.plugins.withType<JacocoPlugin>().configureEach {
            subproject.tasks.matching { it.extensions.findByType<JacocoTaskExtension>() != null }.configureEach {
                val testTask = this
                sourceSets(subproject.sourceSets.main.get())
                executionData(testTask)
            }

            // To automatically run `test` every time `./gradlew codeCoverageReport` is called,
            // you may want to set up a task dependency between them as shown below.
            // Note that this requires the `test` tasks to be resolved eagerly (see `forEach`) which
            // may have a negative effect on the configuration time of your build.
            subproject.tasks.matching { it.extensions.findByType<JacocoTaskExtension>() != null }.forEach {
                rootProject.tasks["codeCoverageReport"].dependsOn(it)
            }
        }
    }

    // enable the different report types (html, xml, csv)
    reports {
        // xml is usually used to integrate code coverage with
        // other tools like SonarQube, Coveralls or Codecov
        xml.required.set(true)

        // HTML reports can be used to see code coverage
        // without any external tools
        html.required.set(true)
    }
}
