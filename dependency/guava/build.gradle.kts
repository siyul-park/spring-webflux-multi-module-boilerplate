val guava_version: String by project

plugins {
    application
}

dependencies {
    api("com.google.guava:guava:$guava_version")
}
