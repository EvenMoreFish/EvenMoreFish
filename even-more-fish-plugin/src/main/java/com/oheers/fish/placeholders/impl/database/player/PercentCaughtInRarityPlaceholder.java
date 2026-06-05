package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.placeholders.abstracted.UniqueFishCaughtProgressPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PercentCaughtInRarityPlaceholder extends UniqueFishCaughtProgressPlaceholder {

    public PercentCaughtInRarityPlaceholder() {
        super("percent_caught_in_rarity_");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NotNull String identifier) {
        ParsedRarityTarget target = parseRarityTarget(player, identifier);
        if (target == null) {
            return null;
        }
        int caught = countCaughtFish(target.uuid(), target.fishList());
        return formatPercent(caught, target.fishList().size());
    }
}
