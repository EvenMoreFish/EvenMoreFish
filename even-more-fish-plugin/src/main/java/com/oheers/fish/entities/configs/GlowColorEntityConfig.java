package com.oheers.fish.entities.configs;

import com.oheers.fish.utils.ScoreboardHelper;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.entity.Entity;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.function.BiConsumer;

public class GlowColorEntityConfig extends EntityConfig<String> {

    public GlowColorEntityConfig(@NotNull Section section) {
        super(section);
    }

    @Override
    public String getConfiguredValue() {
        return section.getString("glow-color");
    }

    @Override
    protected BiConsumer<Entity, String> applyToEntity(@Nullable Map<String, ?> replacements) {
        return (entity, value) -> {
            if (value == null || value.isEmpty()) {
                return;
            }
            ScoreboardHelper.addToTeam(entity, value);
        };
    }
}
