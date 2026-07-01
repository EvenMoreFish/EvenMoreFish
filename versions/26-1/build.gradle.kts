plugins {
    id("java-library")
}

dependencies {
    compileOnly(project(":even-more-fish-plugin"))

    compileOnly(libs.paper.api) {
        version {
            strictly("26.1.1.build.+")
        }
    }
}

tasks {
    jar {
        archiveBaseName.set("26.1")
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