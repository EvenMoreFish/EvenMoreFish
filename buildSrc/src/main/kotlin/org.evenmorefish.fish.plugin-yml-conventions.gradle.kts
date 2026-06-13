import net.minecrell.pluginyml.bukkit.BukkitPluginDescription

plugins {
    id("de.eldoria.plugin-yml.bukkit")
}

afterEvaluate {
    bukkit {
        name = "EvenMoreFish"
        author = "Oheers"
        // This is being used for developers instead of contributors
        contributors = listOf(
            "FireML",
            "sarhatabaot"
        )
        main = "com.oheers.fish.EvenMoreFish"
        version = project.version.toString()
        description = "A fishing extension bringing an exciting new experience to fishing."
        website = "https://github.com/EvenMoreFish/EvenMoreFish"
        apiVersion = "1.20"
        foliaSupported = true

        softDepend = listOf(
            "AuraSkills",
            "Denizen",
            "EcoItems",
            "GriefPrevention",
            "HeadDatabase",
            "ItemsAdder",
            "mcMMO",
            "Nexo",
            "Oraxen",
            "PlayerPoints",
            "PlaceholderAPI",
            "RedProtect",
            "Vault",
            "WorldGuard",
            // VanishChecker dependencies.
            "Essentials",
            "CMI",
            "SayanVanish",
            "AdvancedVanish"
        )
        loadBefore = listOf("AntiAC")

        permissions {
            register("emf.*") {
                children = listOf(
                    "emf.admin",
                    "emf.user"
                )
            }

            register("emf.admin") {
                children = listOf(
                    "emf.admin.update.notify",
                    "emf.admin.migrate"
                )
            }

            register("emf.admin.update.notify") {
                description = "Allows users to be notified about updates."
            }

            register("emf.admin.migrate") {
                description = "Allows users to use the migrate command."
            }

            register("emf.user") {
                children = listOf(
                    "emf.toggle",
                    "emf.top",
                    "emf.shop",
                    "emf.use_rod",
                    "emf.sellall",
                    "emf.help",
                    "emf.next",
                    "emf.applybaits",
                    "emf.journal"
                )
            }

            register("emf.applybaits") {
                description = "Allows users to apply baits to rods."
            }

            register("emf.journal") {
                description = "Allows access to the fish journal."
            }

            register("emf.sellall") {
                description = "Allows users to use sellall."
            }
            register("emf.toggle") {
                description = "Allows users to toggle emf."
            }

            register("emf.top") {
                description = "Allows users to use /emf top."
            }

            register("emf.shop") {
                description = "Allows users to use /emf shop."
            }

            register("emf.use_rod") {
                description = "Allows users to use emf rods."
            }

            register("emf.next") {
                description = "Allows users to see when the next competition will be."
            }

            register("emf.help") {
                description = "Allows users to see the help messages."
                default = BukkitPluginDescription.Permission.Default.TRUE
            }

        }
    }
}