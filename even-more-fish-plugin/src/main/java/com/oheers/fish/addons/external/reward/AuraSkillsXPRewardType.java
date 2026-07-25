package com.oheers.fish.addons.external.reward;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.reward.RewardType;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import dev.aurelium.auraskills.api.registry.NamespacedId;
import dev.aurelium.auraskills.api.skill.Skill;
import dev.aurelium.auraskills.api.skill.Skills;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jspecify.annotations.NonNull;

public class AuraSkillsXPRewardType extends RewardType {

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
        double amount;
        try {
            amount = Double.parseDouble(split[1]);
        } catch (NumberFormatException ex) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid number specified for RewardType " + getIdentifier() + ": " + split[2]);
            return;
        }

        // Handle reward
        AuraSkillsApi api = AuraSkillsApi.get();
        Skill skill;
        // I hate this code, but I don't know a better way.
        try {
            skill = Skills.valueOf(name);
        } catch (IllegalArgumentException ex) {
            try {
                skill = api.getGlobalRegistry().getSkill(NamespacedId.fromString(name));
            } catch (IllegalArgumentException ex2) {
                EvenMoreFish.getInstance().getLogger().warning("Invalid skill specified for RewardType " + getIdentifier() + ": " + name);
                return;
            }
        }
        if (skill == null) {
            EvenMoreFish.getInstance().getLogger().warning("Invalid skill specified for RewardType " + getIdentifier() + ": " + name);
            return;
        }
        api.getUser(player.getUniqueId()).addSkillXp(skill, amount);
    }

    @Override
    public @NonNull String getIdentifier() {
        return "AURASKILLS_XP";
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
