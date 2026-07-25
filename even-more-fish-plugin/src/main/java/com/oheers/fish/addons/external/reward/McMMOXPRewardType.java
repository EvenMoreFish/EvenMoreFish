package com.oheers.fish.addons.external.reward;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.api.exceptions.InvalidSkillException;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

public class McMMOXPRewardType extends RewardType {

    @Override
    public void doReward(@NonNull Player player, @NonNull String key, @NonNull String value, Location hookLocation) {
        // Pre-checks
        String[] split = value.split(",");
        if (split.length < 2) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid format for RewardType " + getIdentifier() + ": " + value);
            EvenMoreFish.getInstance().getLogger().warning("Expected \"Name,Amount\"");
            return;
        }
        String name = split[0];
        int amount;
        try {
            amount = Integer.parseInt(split[1]);
        } catch (NumberFormatException ex) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid number specified for RewardType " + getIdentifier() + ": " + split[1]);
            return;
        }

        // Handle reward
        try {
            // The reason is unknown here as mcMMO doesn't have any relevant reasons.
            ExperienceAPI.addXP(player, name, amount, "unknown");
        } catch (InvalidSkillException ex) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid skill specified for RewardType " + getIdentifier() + ": " + name);
        }
    }

    @Override
    public @NonNull String getIdentifier() {
        return "MCMMO_XP";
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
