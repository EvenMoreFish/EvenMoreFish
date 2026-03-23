package com.oheers.fish.api.addons;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public record AddonMetadata(@NotNull String name, @NotNull String version, @NotNull List<String> authors, String description, String website, List<String> dependencies) {
    @Override
    public @NotNull String toString() {
        return "AddonMetadata{" +
                "name='" + name + '\'' +
                ", version='" + version + '\'' +
                ", authors=" + authors +
                ", description='" + description + '\'' +
                ", website='" + website + '\'' +
                ", dependencies=" + dependencies +
                '}';
    }
}

