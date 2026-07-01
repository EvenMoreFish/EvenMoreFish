plugins {
    id("java-library")
    //id("org.evenmorefish.fish.plugin-yml-conventions")
    //id("org.evenmorefish.fish.shadow-conventions")
    //alias(libs.plugins.run.paper)
}

dependencies {
    compileOnly(project(":even-more-fish-plugin"))

    compileOnly(libs.paper.api) {
        version {
            strictly("26.2.build.+")
        }
    }
}

tasks {
    jar {
        archiveBaseName.set("26.2")
        archiveVersion.set("")
        archiveClassifier.set("")
    }
}
tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}