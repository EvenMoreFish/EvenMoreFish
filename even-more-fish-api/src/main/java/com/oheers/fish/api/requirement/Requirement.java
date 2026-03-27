package com.oheers.fish.api.requirement;

import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.api.registry.EMFRegistry;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Requirement {

    private final Map<String, List<String>> checkMap = new HashMap<>();

    public Requirement() {}

    public Requirement(@NotNull String identifier, @NotNull List<String> values) {
        add(identifier, values);
    }

    public Requirement(@NotNull Map<String, List<String>> requirements) {
        add(requirements);
    }

    public Requirement(@Nullable Section section) {
        add(section);
    }

    public Requirement add(@NotNull String identifier, @NotNull List<String> values) {
        processRequirement(identifier, values);
        return this;
    }

    public Requirement add(@NotNull Map<String, List<String>> requirements) {
        requirements.forEach(this::processRequirement);
        return this;
    }

    public Requirement add(@Nullable Section section) {
        if (section == null) {
            return this;
        }
        section.getRoutesAsStrings(false).forEach(requirementString -> {
            if (section.isList(requirementString)) {
                processRequirement(requirementString, section.getStringList(requirementString));
            } else {
                String value = section.getString(requirementString);
                if (value == null) {
                    return;
                }
                processRequirement(requirementString, List.of(value));
            }
        });
        return this;
    }

    private void processRequirement(@NotNull String identifier, @NotNull List<String> values) {
        this.checkMap.put(identifier, values);
    }

    public boolean meetsRequirements(@NotNull RequirementContext context) {
        for (Map.Entry<String, List<String>> entry : checkMap.entrySet()) {
            String key = entry.getKey().toUpperCase();
            List<String> value = entry.getValue();
            if (key.isEmpty() || value.isEmpty()) {
                EMFPlugin.getInstance().getLogger().warning("Attempted to process an invalid Requirement. Please check for earlier warnings.");
                continue;
            }
            RequirementType requirementType = EMFRegistry.REQUIREMENT_TYPE.get(key);
            if (requirementType == null) {
                EMFPlugin.getInstance().getLogger().warning("Invalid requirement. Possible typo?: " + key);
                continue;
            }
            if (!requirementType.checkRequirement(context, value)) {
                return false;
            }
        }
        return true;
    }

}
