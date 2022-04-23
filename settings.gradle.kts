rootProject.name = "spring-webflux-boilerplate"

fun includeModules(pathname: String) {
    File(pathname).listFiles()?.forEach {
        if (it.isDirectory && File(it, "build.gradle.kts").exists()) {
            include("$pathname:${it.name}")
        }
    }
}

pluginManagement {
    val kotlin_version: String by settings
    val ktlint_version: String by settings
    val spring_boot_version: String by settings
    val spring_dependency_management_version: String by settings

    plugins {
        id("org.springframework.boot") version spring_boot_version
        id("io.spring.dependency-management") version spring_dependency_management_version

        id("org.jetbrains.kotlin.plugin.allopen") version kotlin_version

        kotlin("jvm") version kotlin_version
        kotlin("plugin.spring") version kotlin_version
        kotlin("plugin.jpa") version kotlin_version

        id("org.jlleitschuh.gradle.ktlint") version ktlint_version
    }
}

includeModules("application")
includeModules("domain")
includeModules("common")
includeModules("module")
includeModules("dependency")
