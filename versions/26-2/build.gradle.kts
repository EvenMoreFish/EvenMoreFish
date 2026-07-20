plugins {
    id("java-library")
    id("org.evenmorefish.fish.shadow-conventions")
    alias(libs.plugins.paperweight)
}

dependencies {
    compileOnly(project(":even-more-fish-plugin"))

    paperweight.paperDevBundle("26.2.build.+")
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