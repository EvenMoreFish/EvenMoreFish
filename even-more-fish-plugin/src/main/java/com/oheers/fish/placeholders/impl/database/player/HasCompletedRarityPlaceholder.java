package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.placeholders.abstracted.UniqueFishCaughtProgressPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class HasCompletedRarityPlaceholder extends UniqueFishCaughtProgressPlaceholder {

    public HasCompletedRarityPlaceholder() {
        super("has_completed_rarity_");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier) {
        ParsedRarityTarget target = parseRarityTarget(player, identifier);
        if (target == null) {
            return null;
        }
        return String.valueOf(countCaughtFish(target.uuid(), target.fishList()) >= target.fishList().size());
    }
}
