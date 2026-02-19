package com.oheers.fish.addons.internal.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.reward.RewardType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.potion.PotionEffect;
import org.jetbrains.annotations.NotNull;

public class EffectRewardType extends RewardType {

    @Override
    public void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation) {
        PotionEffect effect = FishUtils.getPotionEffect(value, ",");
        if (effect == null) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid effect specified for RewardType " + getIdentifier() + ": " + value);
            return;
        }
        // Adds a potion effect in accordance to the config.yml "EFFECT:" value
        EvenMoreFish.getScheduler().runTask(player, () -> player.addPotionEffect(effect));
    }

    @Override
    public @NotNull String getIdentifier() {
        return "EFFECT";
    }

    @Override
    public @NotNull String getAuthor() {
        return "Oheers";
    }

    @Override
    public @NotNull JavaPlugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
