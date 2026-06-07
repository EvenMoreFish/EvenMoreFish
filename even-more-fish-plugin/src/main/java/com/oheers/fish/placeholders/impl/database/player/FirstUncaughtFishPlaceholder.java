package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.placeholders.abstracted.UniqueFishCaughtProgressPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class FirstUncaughtFishPlaceholder extends UniqueFishCaughtProgressPlaceholder {

    public FirstUncaughtFishPlaceholder() {
        super("first_uncaught_fish_");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NotNull String identifier) {
        ParsedRarityTarget target = parseRarityTarget(player, identifier);
        if (target == null) {
            return null;
        }

        Fish fish = firstUncaughtFish(target.uuid(), target.fishList());
        return fish == null ? "" : fish.getName();
    }
}
