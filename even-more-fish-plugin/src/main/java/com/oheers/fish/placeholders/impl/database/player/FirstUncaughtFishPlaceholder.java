package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.placeholders.abstracted.UniqueFishCaughtProgressPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class FirstUncaughtFishPlaceholder extends UniqueFishCaughtProgressPlaceholder {

    public FirstUncaughtFishPlaceholder() {
        super("first_uncaught_fish_");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier) {
        ParsedRarityTarget target = parseRarityTarget(player, identifier);
        if (target == null) {
            return null;
        }

        IFish fish = firstUncaughtFish(target.uuid(), target.fishList());
        return fish == null ? "" : fish.getName();
    }
}
