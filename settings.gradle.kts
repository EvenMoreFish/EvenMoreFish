rootProject.name = "even-more-fish"

pluginManagement {
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
    }
}

// Plugin Stuff
include(":even-more-fish-api")
include(":even-more-fish-plugin")

// Addons
include(":addons:even-more-fish-addons-j21")
include(":addons:even-more-fish-addons-crafterfix")

// Versions
include(":versions:1-20")
include(":versions:1-21:1-4") // 1.21.1 to 1.21.4
include(":versions:1-21:5-11") // 1.21.5 to 1.21.11
include(":versions:26-1")
include(":versions:26-2")