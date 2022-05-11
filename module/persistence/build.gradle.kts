dependencies {
    implementation(project(":module:event"))
    implementation(project(":module:data"))
    implementation(project(":module:ulid"))
    implementation(project(":module:util"))

    testImplementation(project(":module:coroutine-test"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
