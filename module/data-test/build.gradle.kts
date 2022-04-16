val junit_version: String by project

dependencies {
    implementation(project(":module:data"))
    implementation(project(":module:coroutine-test"))

    testImplementation(project(":module:ulid"))
    testImplementation(project(":module:util"))
    testImplementation(project(":module:event"))

    implementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
    runtimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit_version")
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
