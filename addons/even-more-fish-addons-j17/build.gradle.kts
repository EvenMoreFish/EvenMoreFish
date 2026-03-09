plugins {
    id("com.oheers.evenmorefish.java-conventions")
    id("com.oheers.evenmorefish.emf-addon")
}

emfAddon {
    name = "Java 17 Addons"
    version = "1.1.0"
    description = "Bundled Java 17 Addons"
    authors = listOf("EvenMoreFish")
    website = "https://github.com/EvenMoreFish/EvenMoreFish"
    dependencies = listOf(
        "Denizen",
        "HeadDatabase",
        "ItemsAdder"
    )
}

repositories {
    maven("https://maven.citizensnpcs.co/repo")
}

dependencies {
    compileOnly(libs.paper.api) {
        version {
            strictly("1.20.1-R0.1-SNAPSHOT")
        }
    }
    compileOnly(libs.denizen.api)
    compileOnly(libs.itemsadder.api)
    compileOnly(libs.headdatabase.api)
    compileOnly(project(":even-more-fish-api"))
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}
