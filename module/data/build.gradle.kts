dependencies {
    implementation(project(":dependency:async"))
    implementation(project(":dependency:r2dbc"))
    implementation(project(":dependency:commons"))
    implementation(project(":dependency:guava"))

    testImplementation(project(":dependency:async-test"))

    implementation(project(":module:been"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")

allOpen {
    annotation("io.github.siyual_park.been.Open")
}
