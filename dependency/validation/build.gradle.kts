val javax_validation_version: String by project
val hibernate_validation_version: String by project
val javax_el_version: String by project

plugins {
    application
}

dependencies {
    api("javax.validation:validation-api:$javax_validation_version")
    api("spring-boot-starter-validation:$hibernate_validation_version")
    api("org.glassfish:javax.el:$javax_el_version")
}
