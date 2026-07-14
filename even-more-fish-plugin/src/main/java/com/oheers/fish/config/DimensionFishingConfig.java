package com.oheers.fish.config;

import com.oheers.fish.api.config.serializer.SoundSerializer;
import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.api.requirement.RequirementContext;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.sound.Sound;
import org.bukkit.entity.Player;
import org.evenmorefish.dimensionfishing.config.DimensionFishingConfigProvider;
import org.evenmorefish.dimensionfishing.util.ParticleFactory;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.function.Predicate;

public class DimensionFishingConfig implements DimensionFishingConfigProvider {

    private static final DimensionFishingConfig INSTANCE = new DimensionFishingConfig();

    private ParticleFactory lavaLureParticles = new ParticleFactory();
    private ParticleFactory voidLureParticles = new ParticleFactory();

    private DimensionFishingConfig() {}

    public static DimensionFishingConfig getInstance() {
        return INSTANCE;
    }

    public void reload() {
        YamlDocument config = MainConfig.getInstance().getConfig();
        this.lavaLureParticles = new ParticleFactory(config.getMapList("dimension-fishing.lava.lure-particles"));
        this.voidLureParticles = new ParticleFactory(config.getMapList("dimension-fishing.void.lure-particles"));
    }

    @Override
    public boolean isLavaEnabled() {
        return MainConfig.getInstance().getConfig().getBoolean("dimension-fishing.lava.enabled", false);
    }

    @Override
    public boolean isVoidEnabled() {
        return MainConfig.getInstance().getConfig().getBoolean("dimension-fishing.void.enabled", false);
    }

    @Override
    public @NotNull List<String> getLavaAllowedWorlds() {
        return MainConfig.getInstance().getConfig().getStringList("dimension-fishing.lava.allowed-worlds");
    }

    @Override
    public @NotNull List<String> getVoidAllowedWorlds() {
        return MainConfig.getInstance().getConfig().getStringList("dimension-fishing.void.allowed-worlds");
    }

    @Override
    public @NotNull Sound getLavaFishingSwallowSound() {
        String soundString = MainConfig.getInstance().getConfig().getString("dimension-fishing.lava.swallow-sound");
        Sound sound = SoundSerializer.get().deserialize(soundString);
        if (sound != null) {
            return sound;
        }
        return Sound.sound().type(org.bukkit.Sound.BLOCK_LAVA_EXTINGUISH).build();
    }

    @Override
    public @NotNull Sound getVoidFishingSwallowSound() {
        String soundString = MainConfig.getInstance().getConfig().getString("dimension-fishing.void.swallow-sound");
        Sound sound = SoundSerializer.get().deserialize(soundString);
        if (sound != null) {
            return sound;
        }
        return Sound.sound().type(org.bukkit.Sound.ENTITY_ENDERMAN_TELEPORT).build();
    }

    @Override
    public @NotNull Sound getLavaFishingBiteSound() {
        String soundString = MainConfig.getInstance().getConfig().getString("dimension-fishing.lava.bite-sound");
        Sound sound = SoundSerializer.get().deserialize(soundString);
        if (sound != null) {
            return sound;
        }
        return Sound.sound()
            .type(org.bukkit.Sound.ENTITY_FISHING_BOBBER_SPLASH)
            .volume(0.25F)
            .pitch(0.5F)
            .build();
    }

    @Override
    public @NotNull Sound getVoidFishingBiteSound() {
        String soundString = MainConfig.getInstance().getConfig().getString("dimension-fishing.void.bite-sound");
        Sound sound = SoundSerializer.get().deserialize(soundString);
        if (sound != null) {
            return sound;
        }
        return Sound.sound()
            .type(org.bukkit.Sound.ENTITY_FOX_BITE)
            .volume(0.25F)
            .pitch(0.1F)
            .build();
    }

    @Override
    public @Nullable String getLavaFishingPermission() {
        String permission = MainConfig.getInstance().getConfig().getString("dimension-fishing.lava.permission", null);
        if ("[]".equals(permission)) {
            return null;
        }
        return permission;
    }

    @Override
    public @Nullable String getVoidFishingPermission() {
        String permission = MainConfig.getInstance().getConfig().getString("dimension-fishing.void.permission", null);
        if ("[]".equals(permission)) {
            return null;
        }
        return permission;
    }

    @Override
    public @NotNull ParticleFactory getLavaFishingLureParticles() {
        return this.lavaLureParticles;
    }

    @Override
    public @NotNull ParticleFactory getVoidFishingLureParticles() {
        return this.voidLureParticles;
    }

    @Override
    public @NotNull Predicate<Player> getLavaPredicate() {
        Section section = MainConfig.getInstance().getConfig().getSection("dimension-fishing.lava.requirements");
        return player -> {
            RequirementContext context = new RequirementContext(
                player.getWorld(),
                player.getLocation(),
                player,
                null,
                null,
                null
            );
            return new Requirement(section).meetsRequirements(context);
        };
    }

    @Override
    public @NotNull Predicate<Player> getVoidPredicate() {
        Section section = MainConfig.getInstance().getConfig().getSection("dimension-fishing.void.requirements");
        return player -> {
            RequirementContext context = new RequirementContext(
                player.getWorld(),
                player.getLocation(),
                player,
                null,
                null,
                null
            );
            return new Requirement(section).meetsRequirements(context);
        };
    }

}
