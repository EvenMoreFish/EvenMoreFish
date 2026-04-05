plugins {
    id("org.evenmorefish.fish.java-conventions")
    id("com.oheers.evenmorefish.emf-addon")
}

emfAddon {
    name = "Modern CMD Addon"
    version = "1.0"
    authors = listOf("EvenMoreFish")
    website = "https://github.com/EvenMoreFish/EvenMoreFish"
    description = "Bundled Modern CustomModelData Addon"
    dependencies = listOf()
}

dependencies {
    compileOnly(libs.paper.api) {
        version {
            strictly("1.21.4-R0.1-SNAPSHOT")
        }
    }
    compileOnly(project(":even-more-fish-plugin"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}
