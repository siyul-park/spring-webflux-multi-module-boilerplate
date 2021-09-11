val coroutines_version: String by project
val projectreactor_version: String by project
val junit_version: String by project

plugins {
    application
}

dependencies {
    api("io.projectreactor:reactor-test:$projectreactor_version")
    api("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_version")

    implementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
    runtimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit_version")
}
