plugins {
    application

    id("org.springframework.boot")
    id("io.spring.dependency-management")
    id("kotlin-spring")

    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":dependency:spring"))
    testImplementation(project(":dependency:spring-test"))
    implementation(project(":dependency:async"))
    testImplementation(project(":dependency:async-test"))
    implementation(project(":dependency:r2dbc"))
    implementation(project(":dependency:kotlinx"))
    implementation(project(":dependency:jwt"))
    implementation(project(":dependency:jackson"))

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

    implementation(project(":domain:user"))
    implementation(project(":domain:client"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
