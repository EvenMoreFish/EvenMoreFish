plugins {
    id("java-library")
    id("org.evenmorefish.fish.shadow-conventions")
    alias(libs.plugins.paperweight)
}

dependencies {
    compileOnly(project(":even-more-fish-plugin"))

    paperweight.paperDevBundle("1.21.5-R0.1-SNAPSHOT")
}

extra["fileName"] = "1.21.5-11"

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}
