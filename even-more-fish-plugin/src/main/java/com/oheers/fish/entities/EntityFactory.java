package com.oheers.fish.entities;

import com.oheers.fish.FishUtils;
import com.oheers.fish.config.ConfigUtils;
import com.oheers.fish.entities.configs.AwareEntityConfig;
import com.oheers.fish.entities.configs.DisplayNameEntityConfig;
import com.oheers.fish.entities.configs.GlowColorEntityConfig;
import com.oheers.fish.entities.configs.GlowingEntityConfig;
import com.oheers.fish.entities.configs.HealthEntityConfig;
import com.oheers.fish.entities.configs.SilentEntityConfig;
import com.oheers.fish.entities.loaders.VanillaEntityLoader;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Location;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.UUID;
import java.util.function.Consumer;

public class EntityFactory {

    private final @NotNull Section configuration;
    private boolean rawEntity = false;
    private UUID relevantPlayer = null;
    private int randomIndex = -1;
    private Consumer<Entity> finalChanges = null;
    private @NotNull EntityLoader entityLoader;

    private final AwareEntityConfig awareness;
    private final DisplayNameEntityConfig displayName;
    private final GlowColorEntityConfig glowColor;
    private final GlowingEntityConfig glowing;
    private final HealthEntityConfig health;
    private final SilentEntityConfig silent;

    private EntityFactory(@NotNull Section initialSection, @Nullable String configLocation) {
        if (configLocation == null) {
            this.configuration = initialSection;
        } else {
            this.configuration = ConfigUtils.getOrCreateSection(initialSection, configLocation);
        }

        this.awareness = new AwareEntityConfig(this.configuration);
        this.displayName = new DisplayNameEntityConfig(this.configuration);
        this.glowColor = new GlowColorEntityConfig(this.configuration);
        this.glowing = new GlowingEntityConfig(this.configuration);
        this.health = new HealthEntityConfig(this.configuration);
        this.silent = new SilentEntityConfig(this.configuration);

        this.entityLoader = getEntityLoader();
    }

    public static EntityFactory entityFactory(@NotNull Section configuration) {
        return new EntityFactory(configuration, null);
    }

    public static EntityFactory entityFactory(@NotNull Section configuration, @Nullable String configLocation) {
        return new EntityFactory(configuration, configLocation);
    }

    public void spawnEntity(@NotNull Location location) {
        spawnEntity(location, (Map<String, ?>) null);
    }

    public void spawnEntity(@NotNull Location location, @Nullable Map<String, ?> replacements) {
        Entity entity = entityLoader.spawn(location);

        if (!rawEntity) {
            awareness.apply(entity, replacements);
            displayName.apply(entity, replacements);
            glowColor.apply(entity, replacements);
            glowing.apply(entity, replacements);
            health.apply(entity, replacements);
            silent.apply(entity, replacements);

            if (finalChanges != null) {
                finalChanges.accept(entity);
            }
        }
    }

    public void spawnEntity(@NotNull Location location, @NotNull UUID relevantPlayer) {
        this.relevantPlayer = relevantPlayer;
        spawnEntity(location);
    }

    public void spawnEntity(@NotNull Location location, @NotNull UUID relevantPlayer, @Nullable Map<String, ?> replacements) {
        this.relevantPlayer = relevantPlayer;
        spawnEntity(location, replacements);
    }

    public @NotNull EntityLoader getEntityLoader() {
        EntityLoader vanilla = getVanillaEntityLoader();
        if (vanilla != null) {
            return vanilla;
        }
        // Add other entity loaders here as needed

        // Default to LLAMA if no loader is found
        return new VanillaEntityLoader(EntityType.LLAMA);
    }

    public boolean isRawEntity() {
        return rawEntity;
    }

    public void setRandomIndex(int randomIndex) {
        this.randomIndex = randomIndex;
        /* TODO implement randomIndex shenanigans
        this.baseItem = getBaseItem();
         */
    }

    public int getRandomIndex() {
        return randomIndex;
    }

    public void setFinalChanges(@Nullable Consumer<Entity> finalChanges) {
        this.finalChanges = finalChanges;
    }

    // Entity Loader Methods

    // Vanilla
    public @Nullable EntityLoader getVanillaEntityLoader() {
        String rawValue = configuration.getString("entity-type");
        if (rawValue == null || rawValue.isEmpty()) {
            return null;
        }
        EntityType entityType = FishUtils.getEnumValue(EntityType.class, rawValue);
        if (entityType == null) {
            return null;
        }
        return new VanillaEntityLoader(entityType);
    }

}
