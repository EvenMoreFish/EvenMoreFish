plugins {
    id("java-library")
    id("org.evenmorefish.fish.shadow-conventions")
}

dependencies {
    compileOnly(project(":even-more-fish-plugin"))

    compileOnly(libs.paper.api) {
        version {
            strictly("26.2.build.+")
        }
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