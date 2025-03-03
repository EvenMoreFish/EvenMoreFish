plugins {
    id("com.oheers.evenmorefish.java-conventions")
}


repositories {
    maven("https://repo.nexomc.com/releases/")
    maven("https://repo.oraxen.com/releases/")
}

dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(libs.nexo)
    compileOnly(libs.oraxen)
    compileOnly(libs.commons.lang3)
    compileOnly(project(":even-more-fish-api"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}
