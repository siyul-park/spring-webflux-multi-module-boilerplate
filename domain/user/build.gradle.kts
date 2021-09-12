plugins {
    application
}

dependencies {
    api(project(":module:data"))
    testImplementation(project(":module:data-test"))

    implementation(project(":dependency:async"))
    implementation(project(":dependency:r2dbc"))
    implementation(project(":dependency:jwt"))

    implementation(project(":domain:auth"))

    testImplementation(project(":dependency:async-test"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
