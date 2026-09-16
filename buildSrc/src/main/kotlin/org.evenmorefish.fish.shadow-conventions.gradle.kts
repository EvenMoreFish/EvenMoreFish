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

        minimize()

        exclude("LICENSE")
        exclude("META-INF/**")

        val plugin: Boolean = (project.findProperty("plugin")?.toString() ?: "false") == "true"
        if (plugin) {
            if (buildNumberOrDate == "RELEASE") {
                archiveFileName.set("EvenMoreFish-${project.version}.jar")
            } else {
                archiveFileName.set("EvenMoreFish-${project.version}-${buildNumberOrDate}.jar")
            }
        } else {
            val name: String? = (project.findProperty("fileName")?.toString())
            if (name != null) {
                archiveFileName.set("$name.jar")
            }
        }
        archiveClassifier.set("")

        relocate("org.bstats", "org.evenmorefish.fish.libs.bstats")
        relocate("de.themoep.inventorygui", "org.evenmorefish.fish.libs.inventorygui")
        relocate("uk.firedev.vanishchecker", "org.evenmorefish.fish.libs.vanishchecker")
        relocate("uk.firedev.daisylib", "org.evenmorefish.fish.libs.daisylib")
        relocate("com.zaxxer", "org.evenmorefish.fish.libs.hikaricp")
        relocate("org.evenmorefish.dimensionfishing", "org.evenmorefish.fish.libs.dimensionfishing")
        relocate("dev.dejvokep.boostedyaml", "org.evenmorefish.fish.libs.boostedyaml")
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