import org.gradle.kotlin.dsl.mavenCentral

rootProject.name = "even-more-fish"

pluginManagement {
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    }
}

// Addons
include(":addons:even-more-fish-addons-j17")
include(":addons:even-more-fish-addons-j21")
include(":addons:even-more-fish-addons-itemmodel")

// Plugin Stuff
include(":even-more-fish-api")
include(":even-more-fish-database-extras")
include(":even-more-fish-plugin") //"core"
include(":even-more-fish-plugin-1-20")
include(":even-more-fish-plugin-1-21")

dependencyResolutionManagement {
    repositories {
        mavenCentral()
        maven("https://eldonexus.de/repository/maven-public/")
        maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") {
            mavenContent {
                snapshotsOnly()
            }
        }
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://jitpack.io")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://github.com/deanveloper/SkullCreator/raw/mvn-repo/")
        maven("https://maven.enginehub.org/repo/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://raw.githubusercontent.com/FabioZumbi12/RedProtect/mvn-repo/")
        maven("https://libraries.minecraft.net/")
        maven("https://nexus.neetgames.com/repository/maven-releases/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://repo.spongepowered.org/maven/")
        maven("https://repo.essentialsx.net/releases/")
        maven("https://repo.auxilor.io/repository/maven-public/")
        maven("https://repo.rosewooddev.io/repository/public/")
        maven("https://repo.minebench.de/")
        maven("https://repo.codemc.io/repository/FireML/")
        maven("https://repo.dmulloy2.net/repository/public/") {
            name = "ProtocolLib Repo - Required by mcMMO"
            mavenContent {
                releasesOnly()
            }
        }
        maven("https://jitpack.io")
        maven("https://repo.alessiodp.com/releases/")
    }
}