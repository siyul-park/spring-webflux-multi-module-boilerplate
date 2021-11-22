dependencies {
    api(project(":dependency:jackson"))
    api(project(":dependency:json-tools"))

    implementation(project(":dependency:async"))
    testImplementation(project(":dependency:async-test"))

    implementation(project(":module:data"))
    testImplementation(project(":module:data-test"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
