plugins {
    id("org.evenmorefish.fish.addon-conventions")
    id("com.oheers.evenmorefish.emf-addon")
}

emfAddon {
    name = "Addon Template"
    version = "1.0"
    authors = listOf("EvenMoreFish")
    website = "https://github.com/EvenMoreFish/EvenMoreFish"
    description = "Addon Template" // Add description here
    dependencies = listOf() // Add plugin names here
}

dependencies {
    compileOnly(libs.paper.api) {
        version {
            strictly("1.21.1-R0.1-SNAPSHOT")
        }
    }
    compileOnly(project(":even-more-fish-api"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}