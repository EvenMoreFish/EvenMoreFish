plugins {
    `java-library`
    `maven-publish`
    `jvm-test-suite`
    alias(libs.plugins.sonar)
    id("org.evenmorefish.fish.shadow-conventions")
    id("org.evenmorefish.fish.publishing-conventions")
    id("org.evenmorefish.fish.plugin-yml-conventions")
}

group = "com.oheers.evenmorefish"
version = properties["project-version"] as String

extra["variant"] = "core"

description = "A fishing extension bringing an exciting new experience to fishing."

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(21))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}


dependencies {
    api(project(":even-more-fish-api"))


    compileOnly(libs.paper.api) {
        version {
            strictly("1.20.1-R0.1-SNAPSHOT")
        }
    }

    compileOnly(libs.vault.api)
    compileOnly(libs.placeholder.api)

    compileOnly(libs.bundles.worldguard) {
        exclude("com.sk89q.worldedit", "worldedit-core")
        exclude("org.spigotmc", "spigot-api")
    }

    compileOnly(libs.bundles.worldedit)
    compileOnly(libs.bundles.redprotect) {
        exclude("net.ess3", "EssentialsX")
        exclude("org.spigotmc", "spigot-api")
        exclude("com.destroystokyo.paper", "paper-api")
        exclude("de.keyle", "mypet")
        exclude("com.sk89q.worldedit", "worldedit-core")
        exclude("com.sk89q.worldedit", "worldedit-bukkit")
        exclude("com.sk89q.worldguard", "worldguard-bukkit")
    }

    compileOnly(libs.aura.skills)

    compileOnly(libs.griefprevention)
    compileOnly(libs.mcmmo) {
        exclude("com.sk89q.worldguard", "worldguard-legacy")
    }
    compileOnly(libs.headdatabase.api)
    compileOnly(libs.playerpoints)

    api(libs.nbt.api)
    api(libs.universalscheduler)

    implementation(libs.bstats)
    implementation(libs.inventorygui)
    implementation(libs.vanishchecker)
    implementation(libs.messagelib)

    implementation(libs.caffeine)
    implementation(libs.jdbi3.core)
    implementation(libs.jdbi3.sqlobject)

    implementation(libs.hikaricp)

    compileOnly(libs.bundles.flyway) {
        exclude("org.xerial", "sqlite-jdbc")
        exclude("com.mysql", "mysql-connector-j")
    }
    compileOnly(libs.friendlyid)
    compileOnly(libs.maven.artifact)
    compileOnly(libs.annotations)
    compileOnly(libs.guava)

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


sonar {
    properties {
        property("sonar.projectKey", "EvenMoreFish_EvenMoreFish")
        property("sonar.organization", "evenmorefish")
        property("sonar.host.url", "https://sonarcloud.io")
    }
}

val copyAddons by tasks.registering(Copy::class) {
    // Make sure the plugin waits for the addons to be built first
    dependsOn(
        ":addons:even-more-fish-addons-j21:build",
        ":addons:even-more-fish-addons-itemmodel:build",
        ":addons:even-more-fish-addons-moderncmd:build",
        ":addons:even-more-fish-addons-crafterfix:build"
    )

    from(project(":addons:even-more-fish-addons-j21").layout.buildDirectory.dir("libs"))
    from(project(":addons:even-more-fish-addons-itemmodel").layout.buildDirectory.dir("libs"))
    from(project(":addons:even-more-fish-addons-moderncmd").layout.buildDirectory.dir("libs"))
    from(project(":addons:even-more-fish-addons-crafterfix").layout.buildDirectory.dir("libs"))

    into(file("src/main/resources/addons"))
}

val copyVersions by tasks.registering(Copy::class) {
    dependsOn(
        ":versions:26-2:build"
    )

    from(project(":versions:26-2").layout.buildDirectory.dir("libs"))

    into(file("src/main/resources/versions"))
}


tasks {
    processResources {
        dependsOn(copyAddons)
        dependsOn(copyVersions)
    }

    clean {
        doFirst {
            val jitpack: Boolean = System.getenv("JITPACK").toBoolean()
            if (jitpack)
                return@doFirst

            for (file in File(project.projectDir, "src/main/resources/addons").listFiles()!!) {
                file.delete()
            }

            for (file in File(project.projectDir, "src/main/resources/versions").listFiles()!!) {
                file.delete()
            }
        }
    }

    compileJava {
        options.compilerArgs.add("-parameters")
        options.isFork = true
        options.encoding = "UTF-8"
    }

}

testing {
    suites {
        val test by getting(JvmTestSuite::class) {
            useJUnitJupiter()

            dependencies {
                implementation(project(":even-more-fish-api"))
                implementation(libs.junit.jupiter.api)
                implementation(libs.mockito.core)
                implementation(libs.boostedyaml)
                implementation(libs.paper.api) {
                    version {
                        strictly("1.20.1-R0.1-SNAPSHOT")
                    }
                }
                runtimeOnly(libs.junit.jupiter.engine)
            }

            targets {
                all {
                    testTask.configure {
                        useJUnitPlatform()
                    }
                }
            }
        }
    }
}

publishing {
    publications {
        create<MavenPublication>("core") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            from(components["java"])
        }
    }
}


