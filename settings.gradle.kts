rootProject.name = "kotlin-boilerplate"

fun includeModules(pathname: String) {
    File(pathname).listFiles()?.forEach {
        if (it.isDirectory && File(it, "build.gradle.kts").exists()) {
            include("$pathname:${it.name}")
        }
    }
}

pluginManagement {
    val klint_version: String by settings
    val kotlin_version: String by settings

    plugins {
        kotlin("jvm") version kotlin_version
        id("org.jlleitschuh.gradle.ktlint") version klint_version
    }
}

includeModules("application")
includeModules("domain")
includeModules("common")
