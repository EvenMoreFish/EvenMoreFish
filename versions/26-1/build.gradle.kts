plugins {
    id("java-library")
    id("org.evenmorefish.fish.shadow-conventions")
    alias(libs.plugins.paperweight)
}

dependencies {
    compileOnly(project(":even-more-fish-plugin"))

    paperweight.paperDevBundle("26.1.2.build.+")
    compileOnly(platform("net.kyori:adventure-bom:4.26.1")) // Manually added because it broke for some reason?

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