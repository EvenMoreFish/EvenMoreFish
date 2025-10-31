import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
    id("com.oheers.evenmorefish.plugin-yml-conventions")
    id("com.oheers.evenmorefish.shadow-conventions")
    alias(libs.plugins.run.paper)
}

group = "com.oheers.evenmorefish"
version = properties["project-version"] as String

extra["variant"] = "1.21"

dependencies {
    api(project(":even-more-fish-plugin"))

    compileOnly(libs.paper.api) {
        version {
            strictly("1.21.1-R0.1-SNAPSHOT")
        }
    }

    library(libs.boostedyaml)
    compileOnlyApi(libs.boostedyaml)

    compileOnly(libs.strokk.annotations)
    annotationProcessor(libs.strokk.processor)
}

afterEvaluate {
    bukkit {
        main = "com.oheers.fish.EMFModule"
        apiVersion = "1.21"
    }
}

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(":even-more-fish-plugin:shadowJar")
    manifest {
        attributes["paperweight-mappings-namespace"] = "mojang"
    }
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    // Quick manual testing, don't use this in ci/cd
    runServer {
        minecraftVersion("1.21.10")
        jvmArgs("-Dcom.mojang.eula.agree=true")
    }
}
tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}