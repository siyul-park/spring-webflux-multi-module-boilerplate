plugins {
    application

    id("org.springframework.boot")
    id("io.spring.dependency-management")

    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":dependency:spring"))
    testImplementation(project(":dependency:spring-test"))

    implementation(project(":dependency:async"))
    testImplementation(project(":dependency:async-test"))

    implementation(project(":dependency:r2dbc"))

    implementation(project(":module:data"))
    implementation(project(":module:been"))
    implementation(project(":module:mapper"))

    implementation(project(":domain:auth"))
    implementation(project(":domain:jackson"))
    implementation(project(":domain:swagger"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
