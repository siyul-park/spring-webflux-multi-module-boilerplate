val junit_version: String by project

dependencies {
    implementation("org.junit.jupiter:junit-jupiter-api:$junit_version")
    runtimeOnly("org.junit.jupiter:junit-jupiter-engine:$junit_version")

    api(project(":dependency:async-test"))

    implementation(project(":module:data"))
    testImplementation(project(":dependency:guava"))

    testImplementation(project(":module:util"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
