plugins {
    application
}

dependencies {
    api(project(":dependency:async"))
    api(project(":dependency:r2dbc"))

    testImplementation(project(":dependency:async-test"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
