package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.Checks;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import org.bukkit.Location;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class RegionRequirementType extends RequirementType {

    @Override
    public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
        Location location = context.getHookOrPlayerLocation();
        if (location == null) {
            EvenMoreFish.getInstance().getLogger().severe("There is no valid location. Failing region Requirement.");
            return false;
        }
        return Checks.canUseRegion(location, values);
    }

    @Override
    public @NonNull String getIdentifier() {
        return "REGION";
    }

    @Override
    public @NonNull String getAuthor() {
        return "Oheers";
    }

    @Override
    public @NonNull Plugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

}
