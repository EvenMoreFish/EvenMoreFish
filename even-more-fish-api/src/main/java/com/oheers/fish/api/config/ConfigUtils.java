package com.oheers.fish.api.config;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

/**
 * For internal use only. This class is used internally and for official addons, and may be changed or removed without notice.
 */
public class ConfigUtils {

    public static @NonNull Section getOrCreateSection(@NonNull Section section, @NonNull String path) {
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
    public static @Nullable Section getSectionOfMany(@NonNull Section section, @NonNull String... paths) {
        for (String path : paths) {
            Section sub = section.getSection(path);
            if (sub != null) {
                return sub;
            }
        }
        return null;
    }

}
