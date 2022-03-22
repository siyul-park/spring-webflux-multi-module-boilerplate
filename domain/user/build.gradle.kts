dependencies {
    implementation(project(":module:data"))
    testImplementation(project(":module:data-test"))
    implementation(project(":module:auth"))
    implementation(project(":module:event"))
    implementation(project(":module:search"))
    implementation(project(":module:persistence"))
    implementation(project(":module:mapper"))

    implementation(project(":domain:client"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
