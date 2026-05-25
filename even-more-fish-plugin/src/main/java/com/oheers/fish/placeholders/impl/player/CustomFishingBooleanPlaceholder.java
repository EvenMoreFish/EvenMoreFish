package com.oheers.fish.placeholders.impl.player;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.placeholders.abstracted.EMFPlaceholder;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomFishingBooleanPlaceholder implements EMFPlaceholder {

    @Override
    public boolean shouldProcess(@NotNull String identifier) {
        return identifier.equalsIgnoreCase("custom_fishing_boolean");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NotNull String identifier) {
        if (!(player instanceof Player online)) {
            return null;
        }
        return String.valueOf(!EvenMoreFish.getInstance().getToggle().isCustomFishingDisabled(online));
    }

}
