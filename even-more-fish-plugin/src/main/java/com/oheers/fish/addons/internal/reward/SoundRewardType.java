package com.oheers.fish.addons.internal.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.reward.RewardType;
import com.oheers.fish.config.serializer.SoundSerializer;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SoundRewardType extends RewardType {

    @Override
    public void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation) {
        Sound sound = SoundSerializer.deserialize(value);
        player.playSound(sound, Sound.Emitter.self());
    }

    @Override
    public @NotNull String getIdentifier() {
        return "SOUND";
    }

    @Override
    public @NotNull String getAuthor() {
        return "FireML";
    }

    @Override
    public @NotNull Plugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
