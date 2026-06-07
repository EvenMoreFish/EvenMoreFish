package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.placeholders.abstracted.UniqueFishCaughtProgressPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class PercentCaughtTotalPlaceholder extends UniqueFishCaughtProgressPlaceholder {

    public PercentCaughtTotalPlaceholder() {
        super("percent_caught_total_");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NotNull String identifier) {
        String target = identifier.substring(getPrefixLength());
        UUID uuid = resolveTarget(player, target, identifier);
        if (uuid == null) {
            return null;
        }

        List<Fish> fishList = getAllFish();
        return formatPercent(countCaughtFish(uuid, fishList), fishList.size());
    }
}
