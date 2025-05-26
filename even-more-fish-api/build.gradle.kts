plugins {
    id("com.oheers.evenmorefish.java-conventions")
}

version = properties["project-version"] as String

dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(libs.annotations)
    compileOnly(libs.commons.lang3)
    compileOnly(libs.universalscheduler)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}
