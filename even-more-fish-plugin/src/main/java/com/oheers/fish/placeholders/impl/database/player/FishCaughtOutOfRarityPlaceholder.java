package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.placeholders.abstracted.UniqueFishCaughtProgressPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.UUID;

public class FishCaughtOutOfRarityPlaceholder extends UniqueFishCaughtProgressPlaceholder {

    public FishCaughtOutOfRarityPlaceholder() {
        super("fish_caught_out_of_rarity_");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NotNull String identifier) {
        String payload = identifier.substring(getPrefixLength());
        int splitIndex = payload.lastIndexOf('_');
        if (splitIndex <= 0 || splitIndex == payload.length() - 1) {
            debug("Placeholder %s received an invalid rarity/target payload for identifier: %s".formatted(getClass().getSimpleName(), identifier));
            return null;
        }

        String rarityId = payload.substring(0, splitIndex);
        String target = payload.substring(splitIndex + 1);

        UUID uuid = resolveTarget(player, target, identifier);
        if (uuid == null) {
            return null;
        }

        Rarity rarity = resolveRarity(rarityId);
        if (rarity == null) {
            debug("Placeholder %s received an invalid rarity '%s' for identifier: %s".formatted(getClass().getSimpleName(), rarityId, identifier));
            return null;
        }

        List<Fish> fishList = rarity.getOriginalFishList();
        return formatProgress(countCaughtFish(uuid, fishList), fishList.size());
    }

    protected @Nullable Rarity resolveRarity(@NotNull String rarityId) {
        return FishManager.getInstance().getRarity(rarityId);
    }
}
