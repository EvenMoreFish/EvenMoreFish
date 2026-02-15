plugins {
    id("com.oheers.evenmorefish.java-conventions")
    id("com.oheers.evenmorefish.publishing-conventions")

    `java-library`
}

group = "com.oheers.evenmorefish"
version = properties["project-version"] as String

dependencies {
    compileOnly(libs.paper.api) {
        version {
            strictly("1.20.1-R0.1-SNAPSHOT")
        }
    }
    compileOnly(libs.annotations)
    compileOnly(libs.universalscheduler)
    compileOnlyApi(libs.boostedyaml)
}


java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications {
        create<MavenPublication>("api") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }
}

tasks.javadoc {
    // Don't fail when missing Javadoc comments
    isFailOnError = false
    // Disable warnings about missing Javadoc comments
    (options as CoreJavadocOptions).apply {
        addBooleanOption("Xdoclint:none", true)
    }
}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()

            dependencies {
                implementation(libs.junit.jupiter.api)
                runtimeOnly(libs.junit.jupiter.engine)
            }

            targets {
                all {
                    testTask.configure {
                        useJUnitPlatform()
                    }
                }
            }
        }
    }
}