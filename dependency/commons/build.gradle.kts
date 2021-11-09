val commons_lang3_version: String by project
val commons_collections4_version: String by project

plugins {
    application
}

dependencies {
    api("org.apache.commons:commons-lang3:$commons_lang3_version")
    api("org.apache.commons:commons-collections4:$commons_collections4_version")
}
