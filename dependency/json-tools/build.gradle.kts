val json_patch_version: String by project

plugins {
    application
}

dependencies {
    api("com.github.java-json-tools:json-patch:$json_patch_version")
}
