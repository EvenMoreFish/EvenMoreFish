package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.placeholders.abstracted.UniqueFishCaughtProgressPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class HasCompletedCollectionPlaceholder extends UniqueFishCaughtProgressPlaceholder {

    public HasCompletedCollectionPlaceholder() {
        super("has_completed_collection_");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier) {
        String target = identifier.substring(getPrefixLength());
        UUID uuid = resolveTarget(player, target, identifier);
        if (uuid == null) {
            return null;
        }

        List<IFish> fishList = getAllFish();
        return String.valueOf(countCaughtFish(uuid, fishList) >= fishList.size());
    }
}
