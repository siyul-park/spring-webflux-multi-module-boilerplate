dependencies {
    implementation(project(":module:ulid"))
    implementation(project(":module:data"))
    implementation(project(":module:event"))
    implementation(project(":module:presentation"))
    implementation(project(":module:persistence"))
    implementation(project(":module:mapper"))

    implementation(project(":domain:auth"))

    testImplementation(project(":module:test"))
    testImplementation(project(":module:coroutine-test"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
