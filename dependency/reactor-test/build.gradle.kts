val projectreactor_version: String by project

plugins {
    application
}

dependencies {
    api("io.projectreactor:reactor-test:$projectreactor_version")
}
