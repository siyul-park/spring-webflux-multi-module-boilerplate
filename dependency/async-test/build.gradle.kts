val coroutines_version: String by project
val projectreactor_version: String by project

plugins {
    application
}

dependencies {
    api("io.projectreactor:reactor-test:$projectreactor_version")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_version")
}
