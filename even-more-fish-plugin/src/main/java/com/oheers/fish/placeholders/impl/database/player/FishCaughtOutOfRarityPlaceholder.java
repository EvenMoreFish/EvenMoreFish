package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.placeholders.abstracted.UniqueFishCaughtProgressPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FishCaughtOutOfRarityPlaceholder extends UniqueFishCaughtProgressPlaceholder {

    public FishCaughtOutOfRarityPlaceholder() {
        super("fish_caught_out_of_rarity_");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier) {
        ParsedRarityTarget target = parseRarityTarget(player, identifier);
        if (target == null) {
            return null;
        }
        return formatProgress(countCaughtFish(target.uuid(), target.fishList()), target.fishList().size());
    }
}
