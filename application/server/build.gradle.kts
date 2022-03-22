plugins {
    application
}

dependencies {
    implementation(project(":domain:user"))
    implementation(project(":domain:client"))

    implementation(project(":module:data"))
    implementation(project(":module:mapper"))
    implementation(project(":module:json"))
    implementation(project(":module:swagger"))
    implementation(project(":module:auth"))
    implementation(project(":module:event"))
    implementation(project(":module:search"))
    implementation(project(":module:validation"))
    implementation(project(":module:util"))
    implementation(project(":module:persistence"))
    implementation(project(":module:search"))

    testImplementation(project(":module:coroutine-test"))
    testImplementation(project(":module:data-test"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
