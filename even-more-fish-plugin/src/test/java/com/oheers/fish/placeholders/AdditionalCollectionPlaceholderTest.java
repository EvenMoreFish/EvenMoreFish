package com.oheers.fish.placeholders;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.placeholders.impl.database.player.DistinctFishCaughtInRarityPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.FirstUncaughtFishPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.HasCaughtFishPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.HasCompletedCollectionPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.HasCompletedRarityPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.PercentCaughtInRarityPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.PercentCaughtTotalPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.RemainingFishInRarityPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.RemainingFishTotalPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TimesCaughtFishPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TotalFishCaughtInRarityPlaceholder;
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

class AdditionalCollectionPlaceholderTest {

    @Test
    void rarityCountPlaceholdersUseDistinctAndRawCountsSeparately() {
        UUID uuid = UUID.randomUUID();
        IFish fishA = fish("legendary_plus", "kraken");
        IFish fishB = fish("legendary_plus", "leviathan");
        IFish fishC = fish("legendary_plus", "serpent");
        Rarity rarity = rarity("legendary_plus", List.of(fishA, fishB, fishC));
        Map<String, Integer> catches = Map.of(key(fishA), 12, key(fishB), 0, key(fishC), 4);

        assertEquals("2", new StubDistinctFishCaughtInRarityPlaceholder(Map.of("legendary_plus", rarity), catches)
            .parsePAPI(offlinePlayer(uuid), "distinct_fish_caught_in_rarity_legendary_plus_player"));
        assertEquals("16", new StubTotalFishCaughtInRarityPlaceholder(Map.of("legendary_plus", rarity), catches)
            .parsePAPI(offlinePlayer(uuid), "total_fish_caught_in_rarity_legendary_plus_player"));
        assertEquals("1", new StubRemainingFishInRarityPlaceholder(Map.of("legendary_plus", rarity), catches)
            .parsePAPI(offlinePlayer(uuid), "remaining_fish_in_rarity_legendary_plus_player"));
        assertEquals("66.7%", new StubPercentCaughtInRarityPlaceholder(Map.of("legendary_plus", rarity), catches)
            .parsePAPI(offlinePlayer(uuid), "percent_caught_in_rarity_legendary_plus_player"));
    }

    @Test
    void completionAndRemainingPlaceholdersUseAllFishCollection() {
        UUID uuid = UUID.randomUUID();
        IFish fishA = fish("common", "salmon");
        IFish fishB = fish("common", "trout");
        IFish fishC = fish("rare", "angler");
        Map<String, Integer> catches = Map.of(key(fishA), 1, key(fishB), 0, key(fishC), 3);

        assertEquals("1", new StubRemainingFishTotalPlaceholder(List.of(fishA, fishB, fishC), catches)
            .parsePAPI(offlinePlayer(uuid), "remaining_fish_total_player"));
        assertEquals("66.7%", new StubPercentCaughtTotalPlaceholder(List.of(fishA, fishB, fishC), catches)
            .parsePAPI(offlinePlayer(uuid), "percent_caught_total_player"));
        assertEquals("false", new StubHasCompletedCollectionPlaceholder(List.of(fishA, fishB, fishC), catches)
            .parsePAPI(offlinePlayer(uuid), "has_completed_collection_player"));
    }

    @Test
    void completionRarityAndFirstUncaughtReflectMissingFish() {
        UUID uuid = UUID.randomUUID();
        IFish fishA = fish("mythic", "kraken");
        IFish fishB = fish("mythic", "leviathan");
        Rarity rarity = rarity("mythic", List.of(fishA, fishB));
        Map<String, Integer> catches = Map.of(key(fishA), 2, key(fishB), 0);

        assertEquals("false", new StubHasCompletedRarityPlaceholder(Map.of("mythic", rarity), catches)
            .parsePAPI(offlinePlayer(uuid), "has_completed_rarity_mythic_player"));
        assertEquals("leviathan", new StubFirstUncaughtFishPlaceholder(Map.of("mythic", rarity), catches)
            .parsePAPI(offlinePlayer(uuid), "first_uncaught_fish_mythic_player"));
    }

    @Test
    void fishSpecificPlaceholdersUseColonSeparatedRarityAndFishIdentifiers() {
        UUID uuid = UUID.randomUUID();
        IFish fish = fish("rare", "angler");
        Map<String, Integer> catches = Map.of(key(fish), 5);

        assertEquals("true", new StubHasCaughtFishPlaceholder(Map.of(key(fish), fish), catches)
            .parsePAPI(offlinePlayer(uuid), "has_caught_rare:angler_player"));
        assertEquals("5", new StubTimesCaughtFishPlaceholder(Map.of(key(fish), fish), catches)
            .parsePAPI(offlinePlayer(uuid), "times_caught_rare:angler_player"));
    }

    @Test
    void fishSpecificPlaceholdersReturnNullForMalformedOrUnknownFishKeys() {
        IFish fish = fish("rare", "angler");

        assertNull(new StubHasCaughtFishPlaceholder(Map.of(key(fish), fish), Map.of())
            .parsePAPI(null, "has_caught_rare_angler_player"));
        assertNull(new StubTimesCaughtFishPlaceholder(Map.of(), Map.of())
            .parsePAPI(null, "times_caught_rare:unknown_00000000-0000-0000-0000-000000000000"));
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
        when(fish.getName()).thenReturn(fishName);
        when(fish.getRarity()).thenReturn(rarity);
        return fish;
    }

    private static Rarity rarity(String rarityId, List<IFish> fishList) {
        Rarity rarity = mock(Rarity.class);
        when(rarity.getId()).thenReturn(rarityId);
        when(rarity.getOriginalFishList()).thenReturn(fishList);
        return rarity;
    }

    private static String key(IFish fish) {
        return fish.getRarity().getId() + ":" + fish.getName();
    }

    private abstract static class StubRarityPlaceholderSupport {
        protected final Map<String, Rarity> rarities;
        protected final Map<String, Integer> catches;

        private StubRarityPlaceholderSupport(Map<String, Rarity> rarities, Map<String, Integer> catches) {
            this.rarities = rarities;
            this.catches = catches;
        }

        protected int distinct(List<? extends IFish> fishList) {
            return (int) fishList.stream().filter(fish -> catches.getOrDefault(key(fish), 0) > 0).count();
        }

        protected int raw(List<? extends IFish> fishList) {
            return fishList.stream().mapToInt(fish -> catches.getOrDefault(key(fish), 0)).sum();
        }
    }

    private static final class StubDistinctFishCaughtInRarityPlaceholder extends DistinctFishCaughtInRarityPlaceholder {
        private final StubRarityPlaceholderSupport support;

        private StubDistinctFishCaughtInRarityPlaceholder(Map<String, Rarity> rarities, Map<String, Integer> catches) {
            this.support = new StubRarityPlaceholderSupport(rarities, catches) {
            };
        }

        @Override
        protected @Nullable Rarity resolveRarity(@NonNull String rarityId) {
            return support.rarities.get(rarityId);
        }

        @Override
        protected int countCaughtFish(@NonNull UUID uuid, @NonNull List<? extends IFish> fishList) {
            return support.distinct(fishList);
        }
    }

    private static final class StubTotalFishCaughtInRarityPlaceholder extends TotalFishCaughtInRarityPlaceholder {
        private final StubRarityPlaceholderSupport support;

        private StubTotalFishCaughtInRarityPlaceholder(Map<String, Rarity> rarities, Map<String, Integer> catches) {
            this.support = new StubRarityPlaceholderSupport(rarities, catches) {
            };
        }

        @Override
        protected @Nullable Rarity resolveRarity(@NonNull String rarityId) {
            return support.rarities.get(rarityId);
        }

        @Override
        protected int totalCaughtQuantity(@NonNull UUID uuid, @NonNull List<? extends IFish> fishList) {
            return support.raw(fishList);
        }
    }

    private static final class StubRemainingFishInRarityPlaceholder extends RemainingFishInRarityPlaceholder {
        private final StubRarityPlaceholderSupport support;

        private StubRemainingFishInRarityPlaceholder(Map<String, Rarity> rarities, Map<String, Integer> catches) {
            this.support = new StubRarityPlaceholderSupport(rarities, catches) {
            };
        }

        @Override
        protected @Nullable Rarity resolveRarity(@NonNull String rarityId) {
            return support.rarities.get(rarityId);
        }

        @Override
        protected int countCaughtFish(@NonNull UUID uuid, @NonNull List<? extends IFish> fishList) {
            return support.distinct(fishList);
        }
    }

    private static final class StubPercentCaughtInRarityPlaceholder extends PercentCaughtInRarityPlaceholder {
        private final StubRarityPlaceholderSupport support;

        private StubPercentCaughtInRarityPlaceholder(Map<String, Rarity> rarities, Map<String, Integer> catches) {
            this.support = new StubRarityPlaceholderSupport(rarities, catches) {
            };
        }

        @Override
        protected @Nullable Rarity resolveRarity(@NonNull String rarityId) {
            return support.rarities.get(rarityId);
        }

        @Override
        protected int countCaughtFish(@NonNull UUID uuid, @NonNull List<? extends IFish> fishList) {
            return support.distinct(fishList);
        }
    }

    private static final class StubRemainingFishTotalPlaceholder extends RemainingFishTotalPlaceholder {
        private final List<IFish> allFish;
        private final Map<String, Integer> catches;

        private StubRemainingFishTotalPlaceholder(List<IFish> allFish, Map<String, Integer> catches) {
            this.allFish = allFish;
            this.catches = catches;
        }

        @Override
        protected @NonNull List<IFish> getAllFish() {
            return allFish;
        }

        @Override
        protected int countCaughtFish(@NonNull UUID uuid, @NonNull List<? extends IFish> fishList) {
            return (int) fishList.stream().filter(fish -> catches.getOrDefault(key(fish), 0) > 0).count();
        }
    }

    private static final class StubPercentCaughtTotalPlaceholder extends PercentCaughtTotalPlaceholder {
        private final List<IFish> allFish;
        private final Map<String, Integer> catches;

        private StubPercentCaughtTotalPlaceholder(List<IFish> allFish, Map<String, Integer> catches) {
            this.allFish = allFish;
            this.catches = catches;
        }

        @Override
        protected @NonNull List<IFish> getAllFish() {
            return allFish;
        }

        @Override
        protected int countCaughtFish(@NonNull UUID uuid, @NonNull List<? extends IFish> fishList) {
            return (int) fishList.stream().filter(fish -> catches.getOrDefault(key(fish), 0) > 0).count();
        }
    }

    private static final class StubHasCompletedCollectionPlaceholder extends HasCompletedCollectionPlaceholder {
        private final List<IFish> allFish;
        private final Map<String, Integer> catches;

        private StubHasCompletedCollectionPlaceholder(List<IFish> allFish, Map<String, Integer> catches) {
            this.allFish = allFish;
            this.catches = catches;
        }

        @Override
        protected @NonNull List<IFish> getAllFish() {
            return allFish;
        }

        @Override
        protected int countCaughtFish(@NonNull UUID uuid, @NonNull List<? extends IFish> fishList) {
            return (int) fishList.stream().filter(fish -> catches.getOrDefault(key(fish), 0) > 0).count();
        }
    }

    private static final class StubHasCompletedRarityPlaceholder extends HasCompletedRarityPlaceholder {
        private final StubRarityPlaceholderSupport support;

        private StubHasCompletedRarityPlaceholder(Map<String, Rarity> rarities, Map<String, Integer> catches) {
            this.support = new StubRarityPlaceholderSupport(rarities, catches) {
            };
        }

        @Override
        protected @Nullable Rarity resolveRarity(@NonNull String rarityId) {
            return support.rarities.get(rarityId);
        }

        @Override
        protected int countCaughtFish(@NonNull UUID uuid, @NonNull List<? extends IFish> fishList) {
            return support.distinct(fishList);
        }
    }

    private static final class StubFirstUncaughtFishPlaceholder extends FirstUncaughtFishPlaceholder {
        private final StubRarityPlaceholderSupport support;

        private StubFirstUncaughtFishPlaceholder(Map<String, Rarity> rarities, Map<String, Integer> catches) {
            this.support = new StubRarityPlaceholderSupport(rarities, catches) {
            };
        }

        @Override
        protected @Nullable Rarity resolveRarity(@NonNull String rarityId) {
            return support.rarities.get(rarityId);
        }

        @Override
        protected boolean hasCaughtFish(@NonNull UUID uuid, @NonNull IFish fish) {
            return support.catches.getOrDefault(key(fish), 0) > 0;
        }
    }

    private static final class StubHasCaughtFishPlaceholder extends HasCaughtFishPlaceholder {
        private final Map<String, IFish> fishMap;
        private final Map<String, Integer> catches;

        private StubHasCaughtFishPlaceholder(Map<String, IFish> fishMap, Map<String, Integer> catches) {
            this.fishMap = fishMap;
            this.catches = catches;
        }

        @Override
        protected @Nullable IFish resolveFish(@NonNull String rarityId, @NonNull String fishName) {
            return fishMap.get(rarityId + ":" + fishName);
        }

        @Override
        protected boolean hasCaughtFish(@NonNull UUID uuid, @NonNull IFish fish) {
            return catches.getOrDefault(key(fish), 0) > 0;
        }
    }

    private static final class StubTimesCaughtFishPlaceholder extends TimesCaughtFishPlaceholder {
        private final Map<String, IFish> fishMap;
        private final Map<String, Integer> catches;

        private StubTimesCaughtFishPlaceholder(Map<String, IFish> fishMap, Map<String, Integer> catches) {
            this.fishMap = fishMap;
            this.catches = catches;
        }

        @Override
        protected @Nullable IFish resolveFish(@NonNull String rarityId, @NonNull String fishName) {
            return fishMap.get(rarityId + ":" + fishName);
        }

        @Override
        protected int timesCaught(@NonNull UUID uuid, @NonNull IFish fish) {
            return catches.getOrDefault(key(fish), 0);
        }
    }
}
