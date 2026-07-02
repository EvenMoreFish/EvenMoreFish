plugins {
    id("java-library")
    id("org.evenmorefish.fish.shadow-conventions")
}

dependencies {
    compileOnly(project(":even-more-fish-plugin"))

    compileOnly(libs.paper.api) {
        version {
            strictly("1.21.1-R0.1-SNAPSHOT")
        }
    }
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}