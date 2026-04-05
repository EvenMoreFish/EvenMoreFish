plugins {
    `maven-publish`
}

publishing {
    repositories {
        maven {
            url = uri("https://repo.codemc.io/repository/EvenMoreFish/")
            val mavenUsername = System.getenv("JENKINS_USERNAME")
            val mavenPassword = System.getenv("JENKINS_PASSWORD")

            if (mavenUsername != null && mavenPassword != null) {
                credentials {
                    username = mavenUsername
                    password = mavenPassword
                }
            }
        }
    }
}
