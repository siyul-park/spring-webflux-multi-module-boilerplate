allprojects {
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
