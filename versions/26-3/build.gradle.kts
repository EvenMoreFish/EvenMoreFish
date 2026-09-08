plugins {
    id("java-library")
    id("org.evenmorefish.fish.shadow-conventions")
    //alias(libs.plugins.paperweight)
}

dependencies {
    compileOnly(project(":even-more-fish-plugin"))

    compileOnly("io.papermc.paper:paper-api:26.3-pre-2.build.0-alpha")
    compileOnly(rootProject.files("temp-libs/paper-26.3.jar")) // DO NOT PUSH JAR FILE TO GITHUB
    //paperweight.paperDevBundle("26.3-pre-2.build.0-alpha")

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