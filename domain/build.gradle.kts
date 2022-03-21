plugins {
    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("kotlin-spring")

    kotlin("plugin.spring")
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}

subprojects {
    apply(plugin = "org.springframework.boot")
    apply(plugin = "io.spring.dependency-management")
    apply(plugin = "org.jetbrains.kotlin.plugin.spring")
    apply(plugin = "kotlin")

    dependencies {
        implementation(project(":dependency:spring"))
        testImplementation(project(":dependency:spring-test"))
    }

    tasks {
        bootJar {
            enabled = false
        }

        jar {
            enabled = true
        }
    }
}
