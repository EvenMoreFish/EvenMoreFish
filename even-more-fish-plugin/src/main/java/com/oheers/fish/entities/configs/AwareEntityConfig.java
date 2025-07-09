package com.oheers.fish.entities.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Mob;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class AwareEntityConfig extends EntityConfig<Boolean> {

    public AwareEntityConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public Boolean getConfiguredValue() {
        return section.getBoolean("aware");
    }

    @Override
    protected BiConsumer<Entity, Boolean> applyToEntity(@Nullable Map<String, ?> replacements) {
        return (entity, value) -> {
            if (value == null) {
                return;
            }
            if (entity instanceof Mob mob) {
                mob.setAware(value);
            }
        };
    }
}
