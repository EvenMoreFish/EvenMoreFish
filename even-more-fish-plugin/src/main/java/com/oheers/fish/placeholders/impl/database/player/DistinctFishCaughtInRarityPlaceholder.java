package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.placeholders.abstracted.UniqueFishCaughtProgressPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class DistinctFishCaughtInRarityPlaceholder extends UniqueFishCaughtProgressPlaceholder {

    public DistinctFishCaughtInRarityPlaceholder() {
        super("distinct_fish_caught_in_rarity_");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier) {
        ParsedRarityTarget target = parseRarityTarget(player, identifier);
        if (target == null) {
            return null;
        }
        return String.valueOf(countCaughtFish(target.uuid(), target.fishList()));
    }
}
