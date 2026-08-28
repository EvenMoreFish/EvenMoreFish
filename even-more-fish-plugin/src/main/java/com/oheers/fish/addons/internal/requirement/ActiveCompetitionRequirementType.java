package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import com.oheers.fish.competition.Competition;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.List;
import java.util.Optional;

public class ActiveCompetitionRequirementType extends RequirementType {

    private static final String NONE = "none";

    /**
     * Checks if a player meets this requirement.
     *
     * @param context The context to check
     * @param values  The values to check this context against
     */
    @Override
    public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
        String id = Optional.ofNullable(Competition.getCurrentlyActive())
            .map(Competition::getCompetitionName)
            .orElse("none");
        boolean match = values.stream().anyMatch(id::equalsIgnoreCase);
        debugLogStatus(match, id);
        return match;
    }

    /**
     * The identifier for this Requirement
     *
     * @return The identifier for this Requirement
     */
    @Override
    public @NonNull String getIdentifier() {
        return "ACTIVE-COMPETITION";
    }

    @Override
    public @NonNull String getAuthor() {
        return "FireML";
    }

    @Override
    public @NonNull Plugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
