package com.oheers.fish.addons.internal.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.reward.RewardType;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

public class SoundRewardType extends RewardType {

    @Override
    public void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation) {
        String[] split = value.split(",");
        Sound sound = FishUtils.getEnumValue(Sound.class, split[0]);
        if (sound == null) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid sound specified for RewardType " + getIdentifier() + ": " + value);
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
        player.playSound(player.getLocation(), sound, volume, pitch);
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
