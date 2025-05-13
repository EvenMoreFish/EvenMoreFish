import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import nu.studer.gradle.jooq.JooqExtension
import org.jooq.meta.jaxb.Property
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*


plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.plugin.yml)
    alias(libs.plugins.shadow)
    alias(libs.plugins.grgit)
    alias(libs.plugins.jooq)
    id("org.sonarqube") version "6.0.1.5171"
}

group = "com.oheers.evenmorefish"
version = properties["project-version"] as String

description = "A fishing extension bringing an exciting new experience to fishing."

repositories {
    mavenCentral()
    maven("https://s01.oss.sonatype.org/content/repositories/snapshots/") // Adventure Snapshots
    maven("https://repo.papermc.io/repository/maven-public/")
    maven("https://github.com/deanveloper/SkullCreator/raw/mvn-repo/")
    maven("https://jitpack.io")
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
    maven("https://repo.essentialsx.net/releases/")
    maven("https://repo.aikar.co/content/groups/aikar/")
}

dependencies {
    api(project(":even-more-fish-api"))

    compileOnly(libs.paper.api)
    compileOnly(libs.vault.api)
    compileOnly(libs.placeholder.api)

    compileOnly(libs.worldguard.core) {
        exclude("com.sk89q.worldedit", "worldedit-core")
    }
    compileOnly(libs.worldguard.bukkit) {
        exclude("org.spigotmc", "spigot-api")
    }
    compileOnly(libs.worldedit.core)
    compileOnly(libs.worldedit.bukkit)

    compileOnly(libs.redprotect.core) {
        exclude("net.ess3", "EssentialsX")
        exclude("org.spigotmc", "spigot-api")
    }
    compileOnly(libs.redprotect.spigot) {
        exclude("net.ess3", "EssentialsX")
        exclude("org.spigotmc", "spigot-api")
        exclude("com.destroystokyo.paper", "paper-api")
        exclude("de.keyle", "mypet")
        exclude("com.sk89q.worldedit", "worldedit-core")
        exclude("com.sk89q.worldedit", "worldedit-bukkit")
        exclude("com.sk89q.worldguard", "worldguard-bukkit")
    }
    compileOnly(libs.aura.skills)
    compileOnly(libs.aurelium.skills)

    compileOnly(libs.griefprevention)
    compileOnly(libs.mcmmo) {
        exclude("com.sk89q.worldguard", "worldguard-legacy")
    }
    compileOnly(libs.headdatabase.api)
    compileOnly(libs.playerpoints)

    implementation(libs.nbt.api)
    implementation(libs.bstats)
    implementation(libs.commandapi)
    implementation(libs.inventorygui)
    implementation(libs.boostedyaml)
    implementation(libs.vanishchecker)
    implementation(libs.universalscheduler)

    library(libs.friendlyid)
    library(libs.flyway.core)
    library(libs.flyway.mysql)
    library(libs.hikaricp)
    library(libs.caffeine)
    library(libs.commons.lang3)
    library(libs.commons.codec)
    library(libs.json.simple)
    library(libs.jooq)
    library(libs.jooq.codegen)
    library(libs.jooq.meta)
    library(libs.connectors.h2)
    library(libs.maven.artifact)

    jooqGenerator(project(":even-more-fish-database-extras"))
    jooqGenerator(libs.jooq.meta.extensions)
    jooqGenerator(libs.connectors.mysql)
}

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
    description = project.description.toString()
    website = "https://github.com/EvenMoreFish/EvenMoreFish"
    apiVersion = "1.20"
    foliaSupported = true

    depend = listOf()
    softDepend = listOf(
        "Vault",
        "PlayerPoints",
        "WorldGuard",
        "PlaceholderAPI",
        "RedProtect",
        "mcMMO",
        "AureliumSkills",
        "AuraSkills",
        "ItemsAdder",
        "Denizen",
        "EcoItems",
        "Oraxen",
        "Nexo",
        "HeadDatabase",
        "GriefPrevention"
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

sonar {
  properties {
    property("sonar.projectKey", "EvenMoreFish_EvenMoreFish")
    property("sonar.organization", "evenmorefish")
    property("sonar.host.url", "https://sonarcloud.io")
  }
}

sourceSets {
    main {
        java.srcDirs.add(File("src/main/generated"))
    }
}
tasks.named("compileJava") {
    dependsOn(":even-more-fish-plugin:generateMysqlJooq")
}

val copyAddons by tasks.registering(Copy::class) {
    // Make sure the plugin waits for the addons to be built first
    dependsOn(":addons:even-more-fish-addons-j17:build", ":addons:even-more-fish-addons-j21:build")

    from(project(":addons:even-more-fish-addons-j17").layout.buildDirectory.dir("libs"))
    from(project(":addons:even-more-fish-addons-j21").layout.buildDirectory.dir("libs"))

    into(file("src/main/resources/addons"))
}

tasks.named("processResources") {
    dependsOn(copyAddons)
}

tasks {
    build {
        dependsOn(
            shadowJar
        )

        doLast {
            val file = project.layout.buildDirectory.file("libs/even-more-fish-plugin-${version}.jar").get()
            file.asFile.delete()
        }


    }

    jooq {
        version.set(libs.versions.jooq)

        val dialects = listOf("mysql")
        val latestSchema = "V8_1__Create_Tables.sql"
        dialects.forEach { dialect ->
            val schemaPath = "src/main/resources/db/migrations/${dialect}/${latestSchema}"
            configureDialect(dialect, schemaPath)
        }
    }

    clean {
        doFirst {
            val jitpack: Boolean = System.getenv("JITPACK").toBoolean()
            if (jitpack)
                return@doFirst

            for (file in File(project.projectDir, "src/main/resources/addons").listFiles()!!) {
                file.delete()
            }

        }

    }

    shadowJar {
        val buildNumberOrDate = getBuildNumberOrDate()
        manifest {
            val buildNumber: String? by project

            attributes["Specification-Title"] = "EvenMoreFish"
            attributes["Specification-Version"] = project.version
            attributes["Implementation-Title"] = grgit.branch.current().name
            attributes["Implementation-Version"] = buildNumberOrDate
            attributes["Database-Baseline-Version"] = "8.0"
        }

        minimize()

        exclude("LICENSE")
        exclude("META-INF/**")

        archiveFileName.set("even-more-fish-${project.version}-${buildNumberOrDate}.jar")
        archiveClassifier.set("shadow")

        relocate("de.tr7zw.changeme.nbtapi", "com.oheers.fish.utils.nbtapi")
        relocate("org.bstats", "com.oheers.fish.libs.bstats")
        relocate("com.github.Anon8281.universalScheduler", "com.oheers.fish.libs.universalScheduler")
        relocate("de.themoep.inventorygui", "com.oheers.fish.libs.inventorygui")
        relocate("uk.firedev.vanishchecker", "com.oheers.fish.libs.vanishchecker")
        relocate("dev.dejvokep.boostedyaml", "com.oheers.fish.libs.boostedyaml")
        relocate("dev.jorel.commandapi", "com.oheers.fish.libs.commandapi")
    }

    compileJava {
        options.compilerArgs.add("-parameters")
        options.isFork = true
        options.encoding = "UTF-8"
    }
}

publishing {
    repositories { // Copied directly from CodeMC's docs
        maven {
            url = uri("https://repo.codemc.io/repository/EvenMoreFish/")

            val mavenUsername = System.getenv("JENKINS_USERNAME")
            val mavenPassword = System.getenv("JENKINS_PASSWORD")

            if (mavenUsername != null && mavenPassword != null) {
                credentials {
                    username = mavenUsername
                    password = mavenPassword
                }
            }
        }
    }
    publications {
        create<MavenPublication>("plugin") {
            groupId = project.group.toString()
            artifactId = project.name
            version = project.version.toString()

            // Publish the fat jar so all relocated dependencies are available
            from(components["shadow"])
        }
    }
}

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(17))
        vendor.set(JvmVendorSpec.ADOPTIUM)
    }
}

sourceSets {
    main {
        java {
            srcDir("src/main/generated")
        }
    }
}

fun getBuildNumberOrDate(): String? {
    val currentBranch = grgit.branch.current().name
    if (currentBranch.equals("head", ignoreCase = true) || currentBranch.equals("master", ignoreCase = true)) {
        val buildNumber: String? by project
        if (buildNumber == null)
            return "RELEASE"

        return buildNumber
    }

    val time = DateTimeFormatter.ofPattern("yyyyMMdd-HHmm", Locale.ENGLISH)
        .withZone(ZoneId.systemDefault())
        .format(Instant.now())

    return time
}

fun JooqExtension.configureDialect(dialect: String, latestSchema: String) {
    configurations {
        create(dialect) {
            generateSchemaSourceOnCompilation.set(false)
            jooqConfiguration.apply {
                jdbc = null
                generator.apply {
                    //https://www.jooq.org/doc/latest/manual/sql-building/dsl-context/custom-settings/settings-parser/
                    strategy.name = "com.oheers.fish.database.extras.PrefixNamingStrategy"
                    database.apply {
                        name = "org.jooq.meta.extensions.ddl.DDLDatabase"
                        properties.add(Property().withKey("scripts").withValue(latestSchema))
                        properties.add(Property().withKey("dialect").withValue(dialect.uppercase()))
                        properties.add(Property().withKey("sort").withValue("flyway"))
                        properties.add(Property().withKey("unqualifiedSchema").withValue("none"))
                    }
                    target.apply {
                        packageName = "com.oheers.fish.database.generated.${dialect}"
                        directory = "src/main/generated/"
                    }
                }
            }
        }
    }
}


