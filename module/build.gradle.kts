plugins {
}

tasks {
    bootJar {
        enabled = false
    }

    jar {
        enabled = true
    }
}

subprojects {
    dependencies {
    }

    tasks {
        bootJar {
            enabled = false
        }

        jar {
            enabled = true
        }
    }
}
