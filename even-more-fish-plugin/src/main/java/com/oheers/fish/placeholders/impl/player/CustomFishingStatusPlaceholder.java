package com.oheers.fish.placeholders.impl.player;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.placeholders.abstracted.EMFPlaceholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CustomFishingStatusPlaceholder implements EMFPlaceholder {

    @Override
    public boolean shouldProcess(@NotNull String identifier) {
        return identifier.equalsIgnoreCase("custom_fishing_status");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable Player player, @NotNull String identifier) {
        if (player == null) {
            return null;
        }
        return EvenMoreFish.getInstance().getToggle().isCustomFishingDisabled(player)
            ? ConfigMessage.CUSTOM_FISHING_DISABLED.getMessage().getLegacyMessage()
            : ConfigMessage.CUSTOM_FISHING_ENABLED.getMessage().getLegacyMessage();
    }

}
