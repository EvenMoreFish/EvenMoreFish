import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
    id("com.oheers.evenmorefish.plugin-yml-conventions")
    id("com.oheers.evenmorefish.shadow-conventions")
    alias(libs.plugins.run.paper)
}

group = "com.oheers.evenmorefish"
version = properties["project-version"] as String

extra["variant"] = "1.20"

afterEvaluate {
    bukkit {
        main = "com.oheers.fish.EMFModule"
    }
}
dependencies {
    compileOnly(libs.paper.api) {
        version {
            strictly("1.20.1-R0.1-SNAPSHOT")
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

    api(project(":even-more-fish-plugin"))
    implementation(libs.commandsapi.bukkit)
}

tasks.named<ShadowJar>("shadowJar") {
    dependsOn(":even-more-fish-plugin:jar")

    from(project(":even-more-fish-plugin").sourceSets["main"].output)
    relocate("dev.jorel.commandapi", "com.oheers.fish.libs.commandapi")
}

tasks {
    build {
        dependsOn(shadowJar)
    }
    // Quick manual testing, don't use this in ci/cd
    runServer {
        minecraftVersion("1.20.1")
        jvmArgs("-Dcom.mojang.eula.agree=true")
    }
}

tasks.test {
    useJUnitPlatform()
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}