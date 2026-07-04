package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import com.oheers.fish.competition.Competition;
import org.bukkit.plugin.Plugin;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ActiveCompetitionRequirementType extends RequirementType {

    /**
     * Checks if a player meets this requirement.
     *
     * @param context The context to check
     * @param values  The values to check this context against
     */
    @Override
    public boolean checkRequirement(@NotNull RequirementContext context, @NotNull List<String> values) {
        Competition active = Competition.getCurrentlyActive();
        if (active == null) {
            return false;
        }
        String id = active.getCompetitionFile().getId();
        return values.stream().anyMatch(id::equalsIgnoreCase);
    }

    /**
     * The identifier for this Requirement
     *
     * @return The identifier for this Requirement
     */
    @Override
    public @NotNull String getIdentifier() {
        return "ACTIVE-COMPETITION";
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
