package com.oheers.fish.entities.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class SilentEntityConfig extends EntityConfig<Boolean> {

    public SilentEntityConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public Boolean getConfiguredValue() {
        return section.getBoolean("silent");
    }

    @Override
    protected BiConsumer<Entity, Boolean> applyToEntity(@Nullable Map<String, ?> replacements) {
        return (entity, value) -> {
            if (value == null) {
                return;
            }
            entity.setSilent(value);
        };
    }

}
