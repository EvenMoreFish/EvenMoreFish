package com.oheers.fish.addons.internal.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import org.bukkit.Location;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

public class HealthRewardType extends RewardType {

    @Override
    public void doReward(@NonNull Player player, @NonNull String key, @NonNull String value, Location hookLocation) {
        double rewardHealth;
        try {
            rewardHealth = Double.parseDouble(value);
        } catch (NumberFormatException ex) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid number specified for RewardType " + getIdentifier() + ": " + value);
            return;
        }
        AttributeInstance attribute = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHealth;
        if (attribute == null) {
            maxHealth = 20.0D;
        } else {
            maxHealth = attribute.getValue();
        }
        double finalHealth = player.getHealth() + rewardHealth;
        if (finalHealth > maxHealth) {
            player.setHealth(maxHealth);
        } else if (finalHealth < 0)  {
            player.setHealth(0);
        } else {
            player.setHealth(finalHealth);
        }
    }

    @Override
    public @NonNull String getIdentifier() {
        return "HEALTH";
    }

    @Override
    public @NonNull String getAuthor() {
        return "Oheers";
    }

    @Override
    public @NonNull JavaPlugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
