rootProject.name = "even-more-fish"

pluginManagement {
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    }
}

// Addons
include(":addons:even-more-fish-addons-j21")
include(":addons:even-more-fish-addons-itemmodel")
include(":addons:even-more-fish-addons-moderncmd")
include(":addons:even-more-fish-addons-crafterfix")

// Versions
include(":versions:1-20")
include(":versions:1-21")
include(":versions:26-1")
include(":versions:26-2")

// Plugin Stuff
include(":even-more-fish-api")
include(":even-more-fish-plugin")