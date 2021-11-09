dependencies {
    api(project(":module:data"))
    testImplementation(project(":module:data-test"))

    implementation(project(":dependency:async"))
    testImplementation(project(":dependency:async-test"))

    implementation(project(":dependency:r2dbc"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
