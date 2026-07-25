package com.oheers.fish.addons.internal.requirement;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.requirement.RequirementType;
import net.milkbowl.vault.permission.Permission;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class GroupRequirementType extends RequirementType {

    private final @NonNull Permission permission;

    public GroupRequirementType(@NonNull Permission permission) {
        this.permission = permission;
    }

    @Override
    public boolean checkRequirement(@NonNull RequirementContext context, @NonNull List<String> values) {
        Player player = context.getPlayer();
        if (player == null) {
            return false;
        }
        for (String value : values) {
            if (permission.playerInGroup(player, value)) {
                debugLogStatus(true, value);
                return true;
            }
        }
        debugLogStatus(false, null);
        return false;
    }

    @Override
    public @NonNull String getIdentifier() {
        return "GROUP";
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
