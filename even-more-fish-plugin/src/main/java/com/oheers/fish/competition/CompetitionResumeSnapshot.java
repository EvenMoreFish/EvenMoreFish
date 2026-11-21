package com.oheers.fish.competition;

import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/** The versioned condition required to resume a competition without a new draw. */
record CompetitionResumeSnapshot(
    String configuredType,
    String resolvedType,
    int numberNeeded,
    long startTimeMillis,
    boolean resumeSupported,
    Map<String, String> typeState
) {

    static final int VERSION = 2;

    void write(YamlDocument config) {
        config.set("snapshot-version", VERSION);
        config.set("configured-type", configuredType);
        config.set("resolved-type", resolvedType);
        config.set("number-needed", numberNeeded);
        config.set("start-time-ms", startTimeMillis);
        config.set("resume-supported", resumeSupported);
        config.set("type-state", new LinkedHashMap<>(typeState));
    }

    static @Nullable CompetitionResumeSnapshot read(YamlDocument config) {
        if (config.getInt("snapshot-version", -1) != VERSION) {
            return null;
        }
        String configured = config.getString("configured-type");
        String resolved = config.getString("resolved-type");
        int numberNeeded = config.getInt("number-needed", Integer.MIN_VALUE);
        long startTimeMillis = config.getLong("start-time-ms", -1L);
        if (configured == null || configured.isBlank() || resolved == null || resolved.isBlank()
            || numberNeeded == Integer.MIN_VALUE || startTimeMillis <= 0) {
            return null;
        }
        Map<String, String> state = new LinkedHashMap<>();
        Section section = config.getSection("type-state");
        if (section != null) {
            for (String key : section.getRoutesAsStrings(false)) {
                String value = section.getString(key);
                if (value == null) {
                    return null;
                }
                state.put(key, value);
            }
        }
        return new CompetitionResumeSnapshot(configured, resolved, numberNeeded,
            startTimeMillis, config.getBoolean("resume-supported"), Map.copyOf(state));
    }
}
