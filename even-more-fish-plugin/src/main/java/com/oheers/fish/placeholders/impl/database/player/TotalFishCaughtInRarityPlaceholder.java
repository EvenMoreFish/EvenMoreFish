package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.placeholders.abstracted.UniqueFishCaughtProgressPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class TotalFishCaughtInRarityPlaceholder extends UniqueFishCaughtProgressPlaceholder {

    public TotalFishCaughtInRarityPlaceholder() {
        super("total_fish_caught_in_rarity_");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NotNull String identifier) {
        ParsedRarityTarget target = parseRarityTarget(player, identifier);
        if (target == null) {
            return null;
        }
        return String.valueOf(totalCaughtQuantity(target.uuid(), target.fishList()));
    }
}
