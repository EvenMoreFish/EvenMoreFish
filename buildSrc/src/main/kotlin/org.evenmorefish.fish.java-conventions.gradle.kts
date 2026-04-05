plugins {
    id("java")
    id("maven-publish")
}

//todo, should be in its own plugin
tasks {
    if (project.name.contains("addons")) {
        jar {
            val addonName = defaultAddonName(project.name)
            archiveFileName.set(addonName)
        }
    }
}

fun defaultAddonName(project: String): String {
    val jvmVersion = project.split("-")[4].uppercase()
    return "EMF-Addons-${jvmVersion}.addon"
}

