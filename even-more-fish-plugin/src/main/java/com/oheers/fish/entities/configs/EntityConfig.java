package com.oheers.fish.entities.configs;

import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public abstract class EntityConfig<T> {

    private T def;
    private T override;
    protected final Section section;
    protected boolean enabled = true;

    public EntityConfig(@NotNull Section section) {
        this.section = section;
    }

    public T getActualValue() {
        if (override != null) {
            return override;
        }
        T configured = getConfiguredValue();
        if (configured == null) {
            return def;
        }
        return configured;
    }

    /**
     * Applies the actual value to the entity if this config is enabled.
     * @param entity The entity to apply the config to.
     */
    public void apply(@NotNull Entity entity, @Nullable Map<String, ?> replacements) {
        if (!enabled) {
            return;
        }
        T value = getActualValue();
        if (value != null) {
            applyToEntity(replacements).accept(entity, value);
        }
    }

    public abstract T getConfiguredValue();

    protected abstract BiConsumer<Entity, T> applyToEntity(@Nullable Map<String, ?> replacements);

    public void setDefault(T def) {
        this.def = def;
    }

    public void setOverride(T override) {
        this.override = override;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

}
