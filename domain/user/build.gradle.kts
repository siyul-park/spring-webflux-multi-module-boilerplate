plugins {
    application
}

dependencies {
    implementation(project(":dependency:async"))
    testImplementation(project(":dependency:async-test"))

    implementation(project(":dependency:r2dbc"))
    implementation(project(":dependency:jwt"))

    implementation(project(":module:data"))
    testImplementation(project(":module:data-test"))
    implementation(project(":module:auth"))
    implementation(project(":module:event"))
    implementation(project(":module:reader"))
    implementation(project(":module:updater"))

    implementation(project(":domain:client"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
