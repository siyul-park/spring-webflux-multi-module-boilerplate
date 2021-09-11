val jjwt_version: String by project

plugins {
    application
}

dependencies {
    api("io.jsonwebtoken:jjwt-api:$jjwt_version")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:$jjwt_version")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:$jjwt_version")
}
