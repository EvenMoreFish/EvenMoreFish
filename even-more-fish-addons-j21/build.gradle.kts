plugins {
    id("com.oheers.evenmorefish.java-conventions")
}


repositories {
    maven("https://repo.nexomc.com/snapshots/")
    maven("https://repo.oraxen.com/releases")
}

dependencies {
    compileOnly(libs.spigot.api)
    compileOnly(libs.nexo)
    compileOnly(libs.oraxen)
    compileOnly(project(":even-more-fish-api"))
}

java.sourceCompatibility = JavaVersion.VERSION_21
