plugins {
    application

    id("org.springframework.boot")
    id("io.spring.dependency-management")

    kotlin("plugin.spring")
}

dependencies {
    implementation(project(":dependency:spring"))
    testImplementation(project(":dependency:spring-test"))

    implementation(project(":dependency:reactor"))
    testImplementation(project(":dependency:reactor-test"))

    implementation(project(":dependency:kotlinx"))

    implementation(project(":domain:jackson"))
    implementation(project(":domain:swagger"))
}

kotlin.sourceSets["main"].kotlin.srcDirs("src/main")
kotlin.sourceSets["test"].kotlin.srcDirs("src/test")

sourceSets["main"].resources.srcDirs("src/main/resources")
sourceSets["test"].resources.srcDirs("src/test/resources")
