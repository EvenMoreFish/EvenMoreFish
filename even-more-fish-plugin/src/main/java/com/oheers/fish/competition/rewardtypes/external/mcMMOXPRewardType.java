package com.oheers.fish.competition.rewardtypes.external;

import com.gmail.nossr50.api.ExperienceAPI;
import com.gmail.nossr50.api.exceptions.InvalidSkillException;
import com.gmail.nossr50.api.exceptions.InvalidXPGainReasonException;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import com.oheers.fish.events.McMMOTreasureEvent;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.Skill;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

public class mcMMOXPRewardType implements RewardType {

    @Override
    public void doReward(@NotNull Player player, @NotNull String key, @NotNull String value, Location hookLocation) {
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
            ExperienceAPI.addXP(player, name, amount, "EvenMoreFish Reward");
        } catch (InvalidSkillException ex) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid skill specified for RewardType " + getIdentifier() + ": " + name);
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "MCMMO_XP";
    }

    @Override
    public @NotNull String getAuthor() {
        return "FireML";
    }

    @Override
    public @NotNull JavaPlugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
