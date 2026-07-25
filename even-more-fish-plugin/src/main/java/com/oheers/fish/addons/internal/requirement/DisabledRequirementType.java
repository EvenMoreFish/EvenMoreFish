package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class DisabledRequirementType extends RequirementType {

    @Override
    public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
        for (String value : values) {
            if (!Boolean.parseBoolean(value)) {
                debugLogStatus(true, null);
                return true;
            }
        }
        debugLogStatus(false, null);
        return false;
    }

    @Override
    public @NonNull String getIdentifier() {
        return "DISABLED";
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
