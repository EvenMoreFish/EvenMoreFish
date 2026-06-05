package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.placeholders.abstracted.UniqueFishCaughtProgressPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class HasCaughtFishPlaceholder extends UniqueFishCaughtProgressPlaceholder {

    public HasCaughtFishPlaceholder() {
        super("has_caught_");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NotNull String identifier) {
        ParsedFishTarget target = parseFishTarget(player, identifier);
        if (target == null) {
            return null;
        }
        return String.valueOf(hasCaughtFish(target.uuid(), target.fish()));
    }
}
