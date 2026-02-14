package com.oheers.fish.api.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * For internal use only. This class is used internally and for official addons, and may be changed or removed without notice.
 */
public class ConfigUtils {

    public static @NotNull Section getOrCreateSection(@NotNull Section section, @NotNull String path) {
        Section subSection = section.getSection(path);
        if (subSection == null) {
            subSection = section.createSection(path);
        }
        return subSection;
    }

    /**
     * Gets the first section of many paths.
     * Useful for typos... Oops
     */
    public static @Nullable Section getSectionOfMany(@NotNull Section section, @NotNull String... paths) {
        for (String path : paths) {
            Section sub = section.getSection(path);
            if (sub != null) {
                return sub;
            }
        }
        return null;
    }

}
