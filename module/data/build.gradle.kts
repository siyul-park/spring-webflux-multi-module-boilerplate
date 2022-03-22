dependencies {
    api(project(":dependency:r2dbc"))
    api(project(":dependency:guava"))
    api(project(":dependency:async"))

    testImplementation(project(":dependency:async-test"))

    implementation(project(":module:event"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
