import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

plugins {
    java
    id("com.gradleup.shadow")
    id("org.ajoberstar.grgit")
}

afterEvaluate {
    tasks.named<ShadowJar>("shadowJar") {
        val buildNumberOrDate = getBuildNumberOrDate()
        val variant: String = project.findProperty("variant")?.toString() ?: "core"
        manifest {
            attributes["Specification-Title"] = "EvenMoreFish"
            attributes["Specification-Version"] = project.version
            attributes["Implementation-Title"] = grgit.branch.current().name
            attributes["Implementation-Version"] = buildNumberOrDate
            attributes["Database-Baseline-Version"] = "8.0"
            attributes["Plugin-Variant"] = variant
        }

        minimize()

        exclude("LICENSE")
        exclude("META-INF/**")

        if (variant == "core") {
            archiveFileName.set("even-more-fish-plugin-${project.version}.jar")
        } else {
            archiveFileName.set("even-more-fish-${project.version}-${variant}-${buildNumberOrDate}.jar")
        }

        archiveClassifier.set("shadow")

        relocate("de.tr7zw.changeme.nbtapi", "com.oheers.fish.utils.nbtapi")
        relocate("org.bstats", "com.oheers.fish.libs.bstats")
        relocate("com.github.Anon8281.universalScheduler", "com.oheers.fish.libs.universalScheduler")
        relocate("de.themoep.inventorygui", "com.oheers.fish.libs.inventorygui")
        relocate("uk.firedev.vanishchecker", "com.oheers.fish.libs.vanishchecker")
        relocate("uk.firedev.messagelib", "com.oheers.fish.libs.messagelib")
        relocate("org.jooq", "com.oheers.fish.libs.jooq")
        relocate("com.zaxxer", "com.oheers.fish.libs.hikaricp")
        relocate("net.byteflux.libby", "com.oheers.fish.libs.libby")
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