dependencies {
    implementation(project(":module:data"))
    implementation(project(":module:ulid"))
    implementation(project(":module:event"))
    implementation(project(":module:persistence"))
    implementation(project(":module:mapper"))

    testImplementation(project(":module:data-test"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
