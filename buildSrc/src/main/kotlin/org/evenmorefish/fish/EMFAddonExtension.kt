package org.evenmorefish.fish

import org.gradle.api.Project
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property

abstract class EMFAddonExtension(project: Project) {
    val name: Property<String> = project.objects.property(String::class.java)
        .convention(project.name)

    val version: Property<String> = project.objects.property(String::class.java)
        .convention(project.version.toString())

    val authors: ListProperty<String> = project.objects.listProperty(String::class.java)
        .convention(emptyList())

    val description: Property<String> = project.objects.property(String::class.java)
        .convention(project.description)

    // Add any additional metadata fields
    val website: Property<String> = project.objects.property(String::class.java)
        .convention("")

    val dependencies: ListProperty<String> = project.objects.listProperty(String::class.java)
        .convention(emptyList())
}