package com.oheers.fish.addons.internal.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.reward.RewardType;
import net.kyori.adventure.sound.Sound;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SoundRewardType extends RewardType {

    @Override
    public void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation) {
        String[] split = value.split(",");
        Sound.Type soundType = FishUtils.getSound(split[0]);
        if (soundType == null) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid sound specified for RewardType " + getIdentifier() + ": " + split[0]);
            return;
        }
        float volume;
        try {
            volume = Float.parseFloat(split[1]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            volume = 1.0f;
        }
        float pitch;
        try {
            pitch = Float.parseFloat(split[2]);
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            pitch = 1.0f;
        }
        Sound sound = Sound.sound()
            .type(soundType)
            .volume(volume)
            .pitch(pitch)
            .build();
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
