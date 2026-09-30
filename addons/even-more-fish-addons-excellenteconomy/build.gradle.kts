plugins {
    id("org.evenmorefish.fish.addon-conventions")
    id("com.oheers.evenmorefish.emf-addon")
}

emfAddon {
    name = "ExcellentEconomy Addon"
    version = "1.0"
    authors = listOf("EvenMoreFish")
    website = "https://github.com/EvenMoreFish/EvenMoreFish"
    description = "Adds an EconomyType for ExcellentEconomy currency."
    dependencies = listOf(
        "ExcellentEconomy"
    )
}

dependencies {
    compileOnly(libs.paper.api) {
        version {
            strictly("1.21.1-R0.1-SNAPSHOT")
        }
    }
    compileOnly(project(":even-more-fish-plugin"))
    compileOnly(libs.excellenteconomy)
    compileOnly(libs.nightcore)
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}