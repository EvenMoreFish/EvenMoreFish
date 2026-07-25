package com.oheers.fish.api.addons;

import org.jspecify.annotations.NonNull;

import java.util.List;

public record AddonMetadata(@NonNull String name, @NonNull String version, @NonNull List<String> authors, String description, String website, List<String> dependencies) {
    @Override
    public @NonNull String toString() {
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

