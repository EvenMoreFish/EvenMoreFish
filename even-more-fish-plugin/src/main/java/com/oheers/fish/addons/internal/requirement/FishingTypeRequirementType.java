package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.fishing.FishingType;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class FishingTypeRequirementType extends RequirementType {

    @Override
    public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
        FishingType contextType = context.getFishingType();
        for (String value : values) {
            FishingType type = FishUtils.getEnumValue(FishingType.class, value);
            if (type != null && contextType == type) {
                debugLogStatus(true, contextType.toString());
                return true;
            }
        }
        debugLogStatus(false, contextType.toString());
        return false;
    }

    @Override
    public @NonNull String getIdentifier() {
        return "FISHING-TYPE";
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
