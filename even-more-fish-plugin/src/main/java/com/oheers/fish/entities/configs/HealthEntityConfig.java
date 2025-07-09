package com.oheers.fish.entities.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class HealthEntityConfig extends EntityConfig<Integer> {

    public HealthEntityConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public Integer getConfiguredValue() {
        return section.getInt("health");
    }

    @Override
    protected BiConsumer<Entity, Integer> applyToEntity(@Nullable Map<String, ?> replacements) {
        return (entity, value) -> {
            if (value == null) {
                return;
            } else if (value <= 0) {
                // Health cannot be less than 1.
                value = 1;
            } else if (value > 2048) {
                // Health cannot be more than 2048.
                value = 2048;
            }
            if (!(entity instanceof LivingEntity livingEntity)) {
                return;
            }
            AttributeInstance attribute = livingEntity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (attribute != null) {
                attribute.setBaseValue(value);
                livingEntity.setHealth(value);
            }
        };
    }

}
