package com.oheers.fish.addons.external.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class PermissionRequirementType extends RequirementType {

    @Override
    public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }
        for (String value : values) {
            if (player.hasPermission(value)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public @NonNull String getIdentifier() {
        return "PERMISSION";
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
