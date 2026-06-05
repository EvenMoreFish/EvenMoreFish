package com.oheers.fish.placeholders;

import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.placeholders.impl.database.player.FishCaughtOutOfRarityPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.FishCaughtOutOfTotalPlaceholder;
import org.bukkit.OfflinePlayer;
import org.junit.jupiter.api.Test;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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
        Fish commonA = fish("common", "salmon");
        Fish commonB = fish("common", "trout");
        Fish rareA = fish("rare", "angler");

        String result = new StubFishCaughtOutOfTotalPlaceholder(
            List.of(commonA, commonB, rareA),
            Map.of(key(commonA), 7, key(commonB), 0, key(rareA), 1)
        ).parsePAPI(offlinePlayer(uuid), "fish_caught_out_of_total_player");

        assertEquals("2/3", result);
    }

    @Test
    void totalPlaceholderReturnsZeroWhenPlayerHasCaughtNothing() {
        UUID uuid = UUID.randomUUID();
        Fish commonA = fish("common", "salmon");
        Fish rareA = fish("rare", "angler");

        String result = new StubFishCaughtOutOfTotalPlaceholder(
            List.of(commonA, rareA),
            Map.of()
        ).parsePAPI(offlinePlayer(uuid), "fish_caught_out_of_total_player");

        assertEquals("0/2", result);
    }

    @Test
    void rarityPlaceholderReturnsDistinctCaughtOutOfRarityTotal() {
        UUID uuid = UUID.randomUUID();
        Fish fishA = fish("legendary_plus", "kraken");
        Fish fishB = fish("legendary_plus", "leviathan");
        Rarity rarity = rarity("legendary_plus", List.of(fishA, fishB));

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

    private static Fish fish(String rarityId, String fishName) {
        Fish fish = mock(Fish.class);
        Rarity rarity = mock(Rarity.class);
        when(rarity.getId()).thenReturn(rarityId);
        when(fish.getName()).thenReturn(fishName);
        when(fish.getRarity()).thenReturn(rarity);
        return fish;
    }

    private static Rarity rarity(String rarityId, List<Fish> fishList) {
        Rarity rarity = mock(Rarity.class);
        when(rarity.getId()).thenReturn(rarityId);
        when(rarity.getOriginalFishList()).thenReturn(fishList);
        return rarity;
    }

    private static String key(Fish fish) {
        return fish.getRarity().getId() + ":" + fish.getName();
    }

    private static final class StubFishCaughtOutOfTotalPlaceholder extends FishCaughtOutOfTotalPlaceholder {
        private final List<Fish> allFish;
        private final Map<String, Integer> caughtFish;

        private StubFishCaughtOutOfTotalPlaceholder(List<Fish> allFish, Map<String, Integer> caughtFish) {
            this.allFish = allFish;
            this.caughtFish = caughtFish;
        }

        @Override
        protected @NotNull List<Fish> getAllFish() {
            return allFish;
        }

        @Override
        protected int countCaughtFish(@NotNull UUID uuid, @NotNull List<Fish> fishList) {
            return (int) fishList.stream()
                .filter(fish -> caughtFish.getOrDefault(key(fish), 0) > 0)
                .count();
        }
    }

    private static final class StubFishCaughtOutOfRarityPlaceholder extends FishCaughtOutOfRarityPlaceholder {
        private final Map<String, Rarity> rarities;
        private final Map<String, Integer> caughtFish;

        private StubFishCaughtOutOfRarityPlaceholder(Map<String, Rarity> rarities, Map<String, Integer> caughtFish) {
            this.rarities = rarities;
            this.caughtFish = caughtFish;
        }

        @Override
        protected @Nullable Rarity resolveRarity(@NotNull String rarityId) {
            return rarities.get(rarityId);
        }

        @Override
        protected int countCaughtFish(@NotNull UUID uuid, @NotNull List<Fish> fishList) {
            return (int) fishList.stream()
                .filter(fish -> caughtFish.getOrDefault(key(fish), 0) > 0)
                .count();
        }
    }
}
