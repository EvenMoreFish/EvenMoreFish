package org.evenmorefish.fish

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.tasks.bundling.Jar
import org.gradle.kotlin.dsl.attributes

class EMFAddonPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("emfAddon", EMFAddonExtension::class.java, project)

        project.tasks.withType(Jar::class.java).configureEach {
            manifest {
                attributes(
                    "name" to extension.name,
                    "version" to extension.version,
                    "authors" to extension.authors,
                    "description" to extension.description,
                    "website" to extension.website,
                    "dependencies" to extension.dependencies
                )
            }
        }
    }
}