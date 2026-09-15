plugins {
    id("java-library")
    id("org.evenmorefish.fish.shadow-conventions")
    alias(libs.plugins.paperweight)
}

dependencies {
    compileOnly(project(":even-more-fish-plugin"))

    paperweight.paperDevBundle("26.2.build.+") // TODO use 26.3 NMS - No real difference for our use but we should use 26.3 in the 26.3 module.

    compileOnly(libs.jspecify)
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