dependencies {
    implementation(project(":module:event"))
    implementation(project(":module:data"))

    implementation(project(":module:ulid"))
    testImplementation(project(":module:coroutine-test"))
    testImplementation(project(":module:data-test"))
    testImplementation(project(":module:util"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
