allprojects {
    repositories {
        // For testing local snapshots.
        //mavenLocal()
        gradlePluginPortal()
        mavenCentral()
        maven("https://repo.papermc.io/repository/maven-public/")
        maven("https://repo.codemc.io/repository/maven-public/")
        maven("https://maven.enginehub.org/repo/")
        maven("https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven("https://raw.githubusercontent.com/FabioZumbi12/RedProtect/mvn-repo/")
        maven("https://repo.codemc.org/repository/maven-public/")
        maven("https://repo.essentialsx.net/releases/")
        maven("https://repo.auxilor.io/repository/maven-public/")
        maven("https://repo.rosewooddev.io/repository/public/")
        maven("https://repo.minebench.de/")
        maven("https://repo.codemc.io/repository/FireML/")
        maven("https://maven.citizensnpcs.co/repo")
        maven("https://nexus.phoenixdevt.fr/repository/maven-public/")
        maven("https://repo.momirealms.net/releases/")
        maven("https://repo.oraxen.com/releases/")
        maven("https://repo.nexomc.com/releases/")

        // This should always be last because it likes to act up.
        maven("https://jitpack.io")
    }
}