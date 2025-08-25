rootProject.name = "even-more-fish"

// Addons
include(":addons:even-more-fish-addons-j17")
include(":addons:even-more-fish-addons-j21")

// Plugin Stuff
include(":even-more-fish-api")
include(":even-more-fish-database-extras")
include(":even-more-fish-plugin")

dependencyResolutionManagement {
    versionCatalogs {
        create("libs") {
            library("paper-api", "io.papermc.paper:paper-api:1.20.1-R0.1-SNAPSHOT")
            library("vault-api", "com.github.MilkBowl:VaultAPI:1.7.1")
            library("placeholder-api", "me.clip:placeholderapi:2.11.6")
            library("bstats", "org.bstats:bstats-bukkit:3.1.0")

            version("worldguard", "7.0.9") // 7.0.9 is the last version that supports 1.20.1
            library("worldguard-core", "com.sk89q.worldguard","worldguard-core").versionRef("worldguard")
            library("worldguard-bukkit", "com.sk89q.worldguard","worldguard-bukkit").versionRef("worldguard")
            bundle("worldguard", listOf("worldguard-core", "worldguard-bukkit"))

            version("worldedit", "7.3.0") // 7.3.0 is the last version that supports 1.20.1 and Java 17
            library("worldedit-core", "com.sk89q.worldedit","worldedit-core").versionRef("worldedit")
            library("worldedit-bukkit", "com.sk89q.worldedit","worldedit-bukkit").versionRef("worldedit")
            bundle("worldedit", listOf("worldedit-core", "worldedit-bukkit"))

            version("redprotect", "7.7.3")
            library("redprotect-core", "br.net.fabiozumbi12.RedProtect","RedProtect-Core").versionRef("redprotect")
            library("redprotect-spigot", "br.net.fabiozumbi12.RedProtect","RedProtect-Spigot-1.13").versionRef("redprotect")
            bundle("redprotect", listOf("redprotect-core", "redprotect-spigot"))

            library("mcmmo", "com.gmail.nossr50.mcMMO:mcMMO:2.2.040")
            library("aurelium-skills", "com.github.Archy-X:AureliumSkills:Beta1.2.8")
            library("aura-skills", "dev.aurelium:auraskills-api-bukkit:2.3.6")
            library("headdatabase-api", "com.arcaniax:HeadDatabase-API:1.3.2")
            library("griefprevention", "com.github.TechFortress:GriefPrevention:16.18.4")

            library("itemsadder-api", "com.github.LoneDev6:API-ItemsAdder:3.6.1")
            library("nbt-api", "de.tr7zw:item-nbt-api:2.15.2-SNAPSHOT")
            library("denizen-api", "com.denizenscript:denizen:1.3.1-SNAPSHOT")
            library("oraxen", "io.th0rgal:oraxen:1.191.0")
            library("nexo", "com.nexomc:nexo:1.9.0")

            version("craftengine", "0.0.61")
            library("craftengine-core", "net.momirealms", "craft-engine-core").versionRef("craftengine")
            library("craftengine-bukkit", "net.momirealms", "craft-engine-bukkit").versionRef("craftengine")
            bundle("craftengine", listOf("craftengine-core", "craftengine-bukkit"))

            library("ecoitems-api", "com.willfp:EcoItems:5.46.0")
            library("ecoitems-libreforge", "com.willfp:libreforge:4.21.1")
            library("ecoitems-eco", "com.willfp:eco:6.56.0")
            bundle("ecoitems", listOf("ecoitems-api", "ecoitems-libreforge", "ecoitems-eco"))

            library("caffeine", "com.github.ben-manes.caffeine:caffeine:3.2.2")
            library("annotations", "org.jetbrains:annotations:26.0.2")

            version("flyway", "10.22.0")
            library("flyway-core", "org.flywaydb","flyway-core").versionRef("flyway")
            library("flyway-mysql", "org.flywaydb","flyway-mysql").versionRef("flyway")
            bundle("flyway", listOf("flyway-core", "flyway-mysql"))

            library("friendlyid", "com.devskiller.friendly-id:friendly-id:1.1.0")
            library("hikaricp", "com.zaxxer:HikariCP:7.0.1")
            library("json-simple", "com.googlecode.json-simple:json-simple:1.1.1")

            library("universalscheduler", "com.github.Anon8281:UniversalScheduler:0.1.7")
            library("playerpoints", "org.black_ixx:playerpoints:3.3.2")

            library("vanishchecker", "uk.firedev:VanishChecker:1.0.5")

            library("commandapi", "dev.jorel:commandapi-bukkit-shade:10.1.2")
            library("inventorygui", "de.themoep:inventorygui:1.6.4-SNAPSHOT")

            plugin("shadow", "com.gradleup.shadow").version("9.0.0")
            plugin("plugin-yml", "de.eldoria.plugin-yml.bukkit").version("0.8.0")

            library("boostedyaml", "dev.dejvokep:boosted-yaml:1.3.7")

            plugin("grgit", "org.ajoberstar.grgit").version("5.3.2")

            version("jooq", "3.19.24")
            library("jooq", "org.jooq","jooq").versionRef("jooq")
            library("jooq-codegen", "org.jooq", "jooq-codegen").versionRef("jooq")
            library("jooq-meta", "org.jooq", "jooq-meta").versionRef("jooq")
            library("jooq-meta-extensions","org.jooq","jooq-meta-extensions").versionRef("jooq")
            plugin("jooq", "nu.studer.jooq").version("9.0")

            library("connectors-mysql", "com.mysql:mysql-connector-j:9.4.0")
            library("connectors-sqlite", "org.xerial:sqlite-jdbc:3.47.0.0")
            library("connectors-h2", "com.h2database:h2:2.3.232")
            bundle("connectors", listOf("connectors-mysql", "connectors-sqlite"))
            library("maven-artifact", "org.apache.maven:maven-artifact:4.0.0-rc-4")

            version("junit", "5.13.4")
            library("junit-jupiter-api", "org.junit.jupiter","junit-jupiter-api").versionRef("junit")
            library("junit-jupiter-engine","org.junit.jupiter","junit-jupiter-engine").versionRef("junit")

            library("guava", "com.google.guava:guava:33.4.8-jre")
        }
    }
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}
