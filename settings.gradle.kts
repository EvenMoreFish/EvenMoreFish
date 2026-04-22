rootProject.name = "even-more-fish"

pluginManagement {
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    }
}

// Addons
include(":addons:even-more-fish-addons-j21")
include(":addons:even-more-fish-addons-itemmodel")
include("addons:even-more-fish-addons-moderncmd")

// Plugin Stuff
include(":even-more-fish-api")
include(":even-more-fish-plugin") //"core"
include(":even-more-fish-plugin-1-20")
include(":even-more-fish-plugin-1-21")
include("even-more-fish-plugin-26-1")