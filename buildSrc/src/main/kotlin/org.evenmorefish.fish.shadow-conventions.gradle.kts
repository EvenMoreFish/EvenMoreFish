import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.*

plugins {
    java
    id("com.gradleup.shadow")
    id("org.ajoberstar.grgit")
}

afterEvaluate {
    tasks.named<ShadowJar>("shadowJar") {
        val buildNumberOrDate = getBuildNumberOrDate()

        manifest {
            attributes["Specification-Title"] = "EvenMoreFish"
            attributes["Specification-Version"] = project.version
            attributes["Implementation-Title"] = grgit.branch.current().name
            attributes["Implementation-Version"] = buildNumberOrDate
            attributes["Database-Baseline-Version"] = "8.0"
        }

        minimize {
            exclude(dependency("dev.jorel:.*:.*"))
        }

        exclude("LICENSE")
        exclude("META-INF/**")

        val plugin: Boolean = (project.findProperty("plugin")?.toString() ?: "false") == "true"
        if (plugin) {
            if (buildNumberOrDate == "RELEASE") {
                archiveFileName.set("EvenMoreFish-${project.version}.jar")
            } else {
                archiveFileName.set("EvenMoreFish-${project.version}-${buildNumberOrDate}.jar")
            }
        }
        archiveClassifier.set("")

        relocate("de.tr7zw.changeme.nbtapi", "com.oheers.fish.utils.nbtapi")
        relocate("org.bstats", "com.oheers.fish.libs.bstats")
        relocate("com.github.Anon8281.universalScheduler", "com.oheers.fish.libs.universalScheduler")
        relocate("de.themoep.inventorygui", "com.oheers.fish.libs.inventorygui")
        relocate("uk.firedev.vanishchecker", "com.oheers.fish.libs.vanishchecker")
        relocate("uk.firedev.messagelib", "com.oheers.fish.libs.messagelib")
        relocate("org.jooq", "com.oheers.fish.libs.jooq")
        relocate("com.zaxxer", "com.oheers.fish.libs.hikaricp")
        relocate("dev.jorel.commandapi", "com.oheers.fish.libs.commandapi")
        relocate("org.evenmorefish.dimensionfishing", "com.oheers.fish.libs.dimensionfishing")
    }
    tasks.named<Jar>("jar") {
        enabled = false
    }
}



private fun getBuildNumberOrDate(): String? {
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