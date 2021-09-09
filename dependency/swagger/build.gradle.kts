val springfox_version: String by project

plugins {
    application
}

dependencies {
    api("io.springfox:springfox-boot-starter:$springfox_version")
    api("io.springfox:springfox-swagger-ui:$springfox_version")
}
