package com.oheers.fish.placeholders;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.placeholders.impl.database.player.FishCaughtOutOfRarityPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.FishCaughtOutOfTotalPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class UniqueFishCaughtProgressPlaceholderTest {

    @Test
    void totalPlaceholderReturnsDistinctCaughtOutOfTotalFish() {
        UUID uuid = UUID.randomUUID();
        IFish commonA = fish("common", "salmon");
        IFish commonB = fish("common", "trout");
        IFish rareA = fish("rare", "angler");

        String result = new StubFishCaughtOutOfTotalPlaceholder(
            List.of(commonA, commonB, rareA),
            Map.of(key(commonA), 7, key(commonB), 0, key(rareA), 1)
        ).parsePAPI(offlinePlayer(uuid), "fish_caught_out_of_total_player");

        assertEquals("2/3", result);
    }

    @Test
    void totalPlaceholderReturnsZeroWhenPlayerHasCaughtNothing() {
        UUID uuid = UUID.randomUUID();
        IFish commonA = fish("common", "salmon");
        IFish rareA = fish("rare", "angler");

        String result = new StubFishCaughtOutOfTotalPlaceholder(
            List.of(commonA, rareA),
            Map.of()
        ).parsePAPI(offlinePlayer(uuid), "fish_caught_out_of_total_player");

        assertEquals("0/2", result);
    }

    @Test
    void rarityPlaceholderReturnsDistinctCaughtOutOfRarityTotal() {
        UUID uuid = UUID.randomUUID();
        IFish fishA = fish("legendary_plus", "kraken");
        IFish fishB = fish("legendary_plus", "leviathan");
        IRarity rarity = rarity("legendary_plus", List.of(fishA, fishB));

        String result = new StubFishCaughtOutOfRarityPlaceholder(
            Map.of("legendary_plus", rarity),
            Map.of(key(fishA), 12, key(fishB), 0)
        ).parsePAPI(offlinePlayer(uuid), "fish_caught_out_of_rarity_legendary_plus_player");

        assertEquals("1/2", result);
    }

    @Test
    void rarityPlaceholderReturnsNullForInvalidRarity() {
        String result = new StubFishCaughtOutOfRarityPlaceholder(Map.of(), Map.of())
            .parsePAPI(null, "fish_caught_out_of_rarity_missing_00000000-0000-0000-0000-000000000000");

        assertNull(result);
    }

    @Test
    void totalPlaceholderReturnsNullForInvalidPlayerTarget() {
        String result = new StubFishCaughtOutOfTotalPlaceholder(List.of(), Map.of())
            .parsePAPI(null, "fish_caught_out_of_total_player");

        assertNull(result);
    }

    @Test
    void rarityPlaceholderReturnsNullForMalformedPayload() {
        String result = new StubFishCaughtOutOfRarityPlaceholder(Map.of(), Map.of())
            .parsePAPI(null, "fish_caught_out_of_rarity_common");

        assertNull(result);
    }

    private static OfflinePlayer offlinePlayer(UUID uuid) {
        OfflinePlayer player = mock(OfflinePlayer.class);
        when(player.getUniqueId()).thenReturn(uuid);
        return player;
    }

    private static IFish fish(String rarityId, String fishName) {
        IFish fish = mock(IFish.class);
        IRarity rarity = mock(IRarity.class);
        when(rarity.getId()).thenReturn(rarityId);
        when(fish.getId()).thenReturn(fishName);
        when(fish.getRarity()).thenReturn(rarity);
        return fish;
    }

    private static IRarity rarity(String rarityId, List<IFish> fishList) {
        // This needs to be Rarity because dumb.
        Rarity rarity = mock(Rarity.class);
        when(rarity.getId()).thenReturn(rarityId);
        when(rarity.getOriginalFishList()).thenReturn(fishList);
        return rarity;
    }

    private static String key(IFish fish) {
        return fish.getRarity().getId() + ":" + fish.getId();
    }

    private static final class StubFishCaughtOutOfTotalPlaceholder extends FishCaughtOutOfTotalPlaceholder {
        private final List<IFish> allFish;
        private final Map<String, Integer> caughtFish;

        private StubFishCaughtOutOfTotalPlaceholder(List<IFish> allFish, Map<String, Integer> caughtFish) {
            this.allFish = allFish;
            this.caughtFish = caughtFish;
        }

        @Override
        protected @NonNull List<IFish> getAllFish() {
            return allFish;
        }

        @Override
        protected int countCaughtFish(@NonNull UUID uuid, @NonNull List<? extends IFish> fishList) {
            return (int) fishList.stream()
                .filter(fish -> caughtFish.getOrDefault(key(fish), 0) > 0)
                .count();
        }
    }

    private static final class StubFishCaughtOutOfRarityPlaceholder extends FishCaughtOutOfRarityPlaceholder {
        private final Map<String, IRarity> rarities;
        private final Map<String, Integer> caughtFish;

        private StubFishCaughtOutOfRarityPlaceholder(Map<String, IRarity> rarities, Map<String, Integer> caughtFish) {
            this.rarities = rarities;
            this.caughtFish = caughtFish;
        }

        @Override
        protected @Nullable IRarity resolveRarity(@NonNull String rarityId) {
            return rarities.get(rarityId);
        }

        @Override
        protected int countCaughtFish(@NonNull UUID uuid, @NonNull List<? extends IFish> fishList) {
            return (int) fishList.stream()
                .filter(fish -> caughtFish.getOrDefault(key(fish), 0) > 0)
                .count();
        }
    }
}
