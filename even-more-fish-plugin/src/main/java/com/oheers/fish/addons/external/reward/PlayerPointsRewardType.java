package com.oheers.fish.addons.external.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import org.black_ixx.playerpoints.PlayerPoints;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

public class PlayerPointsRewardType extends RewardType {

    @Override
    public void doReward(@NonNull Player player, @NonNull String key, @NonNull String value, Location hookLocation) {
        int amount;
        try {
            amount = Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid number specified for RewardType " + getIdentifier() + ": " + value);
            return;
        }
        if (EvenMoreFish.getInstance().getDependencyManager().isUsingPlayerPoints()) {
            PlayerPoints.getInstance().getAPI().give(player.getUniqueId(), amount);
        }
    }

    @Override
    public @NonNull String getIdentifier() {
        return "PLAYER_POINTS";
    }

    @Override
    public @NonNull String getAuthor() {
        return "FireML";
    }

    @Override
    public @NonNull JavaPlugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
