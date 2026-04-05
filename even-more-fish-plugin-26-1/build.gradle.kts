import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
    id("org.evenmorefish.fish.plugin-yml-conventions")
    id("org.evenmorefish.fish.shadow-conventions")
    alias(libs.plugins.run.paper)
}

group = "com.oheers.evenmorefish"
version = properties["project-version"] as String

extra["variant"] = "26.1"

dependencies {
    api(project(":even-more-fish-plugin"))

    compileOnly(libs.paper.api) {
        version {
            strictly("26.1.1.build.+")
        }
    }

    library(libs.bundles.flyway) {
        exclude("org.xerial", "sqlite-jdbc")
        exclude("com.mysql", "mysql-connector-j")
    }
    library(libs.friendlyid)
    library(libs.maven.artifact)
    library(libs.annotations)
    library(libs.guava)

    library(libs.boostedyaml)
    compileOnlyApi(libs.boostedyaml)

    library(libs.bundles.connectors)
}

afterEvaluate {
    bukkit {
        main = "com.oheers.fish.EMFModule"
        apiVersion = "26.1"
    }
}

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(":even-more-fish-plugin:jar")
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
        minecraftVersion("26.1.1")
        jvmArgs("-Dcom.mojang.eula.agree=true")
    }
}
tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}