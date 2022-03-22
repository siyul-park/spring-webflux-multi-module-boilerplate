val junit_version: String by project
val projectreactor_version: String by project
val coroutines_version: String by project

dependencies {
    implementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
    runtimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit_version")

    implementation("io.projectreactor:reactor-test:$projectreactor_version")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$coroutines_version")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
