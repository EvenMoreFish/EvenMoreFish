package com.oheers.fish.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;

public class ConfigUtils {

    public static @NotNull Section getOrCreateSection(@NotNull Section section, @NotNull String path) {
        Section subSection = section.getSection(path);
        if (subSection == null) {
            subSection = section.createSection(path);
        }
        return subSection;
    }

}
