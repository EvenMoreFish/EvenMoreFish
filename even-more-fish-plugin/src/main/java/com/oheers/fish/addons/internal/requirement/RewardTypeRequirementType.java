package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.registry.EMFRegistry;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

/**
 * A RequirementType to check that specific RewardTypes are loaded.
 */
public class RewardTypeRequirementType extends RequirementType {

    /**
     * Checks if a player meets this requirement.
     *
     * @param context The context to check
     * @param values  The values to check this context against
     */
    @Override
    public boolean checkRequirement(@NotNull RequirementContext context, @NotNull List<String> values) {
        return values.stream()
            .filter(Objects::nonNull)
            .allMatch(value -> EMFRegistry.REWARD_TYPE.get(value) != null);
    }

    /**
     * The identifier for this Requirement
     *
     * @return The identifier for this Requirement
     */
    @Override
    public @NotNull String getIdentifier() {
        return "REWARD-TYPE";
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
