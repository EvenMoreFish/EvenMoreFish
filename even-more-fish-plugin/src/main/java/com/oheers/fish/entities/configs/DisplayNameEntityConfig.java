package com.oheers.fish.entities.configs;

import com.oheers.fish.messages.EMFSingleMessage;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class DisplayNameEntityConfig extends EntityConfig<String> {

    public DisplayNameEntityConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public String getConfiguredValue() {
        return section.getString("displayname");
    }

    @Override
    protected BiConsumer<Entity, String> applyToEntity(@Nullable Map<String, ?> replacements) {
        return (entity, value) -> {
            if (value == null || value.isEmpty()) {
                return;
            }
            EMFSingleMessage display = EMFSingleMessage.fromString(value);
            display.setVariables(replacements);
            entity.customName(display.getComponentMessage());
            entity.setCustomNameVisible(true);
        };
    }

}
