package com.oheers.fish.addons.internal.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import com.oheers.fish.api.config.serializer.SoundSerializer;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

public class SoundRewardType extends RewardType {

    @Override
    public void doReward(@NonNull Player player, @NonNull String key, @NonNull String value, Location hookLocation) {
        Sound sound = SoundSerializer.get().deserialize(value);
        player.playSound(sound, Sound.Emitter.self());
    }

    @Override
    public @NonNull String getIdentifier() {
        return "SOUND";
    }

    @Override
    public @NonNull String getAuthor() {
        return "FireML";
    }

    @Override
    public @NonNull Plugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
