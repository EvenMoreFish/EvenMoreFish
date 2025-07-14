plugins {
    id("com.oheers.evenmorefish.java-conventions")
    id("com.oheers.evenmorefish.emf-addon")
}

emfAddon {
    name = "Java 21 Addons"
    version = "1.1.0"
    authors = listOf("EvenMoreFish")
    website = "https://github.com/EvenMoreFish/EvenMoreFish"
    description = "Bundled Java 21 Addons"
    dependencies = listOf(
        "CraftEngine",
        "Nexo",
        "Oraxen"
    )
}

repositories {
    maven("https://repo.nexomc.com/releases/")
    maven("https://repo.oraxen.com/releases/")
    maven("https://repo.momirealms.net/releases/")
}

dependencies {
    compileOnly(libs.paper.api)
    compileOnly(libs.nexo)
    compileOnly(libs.oraxen)
    compileOnly(libs.bundles.craftengine)
    compileOnly(project(":even-more-fish-api"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}
