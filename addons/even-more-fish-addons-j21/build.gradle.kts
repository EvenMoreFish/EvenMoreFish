plugins {
    id("org.evenmorefish.fish.java-conventions")
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
        "EcoItems",
        "Nexo",
        "Oraxen"
    )
}

dependencies {
    compileOnly(libs.paper.api) {
        version {
            strictly("1.20.1-R0.1-SNAPSHOT")
        }
    }
    compileOnly(libs.nexo)
    compileOnly(libs.oraxen)
    compileOnly(libs.bundles.craftengine)
    compileOnly(libs.ecoitems)
    compileOnly("com.willfp:libreforge:4.81.0:all")
    compileOnly(libs.eco)
    compileOnly(libs.denizen.api)
    compileOnly(libs.itemsadder.api)
    compileOnly(libs.headdatabase.api)
    compileOnly(libs.mmoitems.api)
    compileOnly(libs.mythic.lib)
    compileOnly(project(":even-more-fish-api"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}
