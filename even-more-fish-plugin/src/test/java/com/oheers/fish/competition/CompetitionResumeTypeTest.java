package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.fishing.items.RarityKey;
import com.oheers.fish.competition.types.RandomCompetitionType;
import com.oheers.fish.competition.types.LargestFishCompetitionType;
import com.oheers.fish.competition.types.SpecificFishCompetitionType;
import com.oheers.fish.competition.types.SpecificRarityCompetitionType;
import com.oheers.fish.fishing.items.FishManager;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.MockedStatic;

import java.nio.file.Path;
import java.time.Instant;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class CompetitionResumeTypeTest {

    @TempDir
    Path directory;

    @Test
    void restoredFishTargetSurvivesBeginValidationIncludingRandomForwarding() {
        SpecificFishCompetitionType type = new SpecificFishCompetitionType();
        RarityKey key = mock(RarityKey.class);
        IFish fish = mock(IFish.class);
        when(key.getFish()).thenReturn(fish);
        when(key.toString()).thenReturn("legendary:golden_cod");
        when(fish.getRarityKey()).thenReturn(key);

        try (MockedStatic<RarityKey> rarityKeys = mockStatic(RarityKey.class);
             MockedStatic<EvenMoreFish> plugin = mockPlugin()) {
            rarityKeys.when(() -> RarityKey.of("legendary:golden_cod")).thenReturn(key);
            Competition competition = new Competition(60, type);
            competition.setNumberNeeded(4);

            assertTrue(type.restoreResumeState(Map.of("selected-fish", "legendary:golden_cod"), competition));
            assertTrue(type.isUsable(competition));
            assertSame(fish, type.getSelectedFish());
            assertEquals(Map.of("selected-fish", "legendary:golden_cod"), type.saveResumeState());
            assertNotSame(type, type.freshInstance());
            assertFalse(type.freshInstance().restoreResumeState(Map.of("selected-fish", "missing:fish"), competition));

            CompetitionType.Forwarding random = new CompetitionType.Forwarding(new RandomCompetitionType(), type);
            assertSame(type, random.getResolvedType());
            assertEquals(type.saveResumeState(), random.saveResumeState());
        }
    }

    @Test
    void restoredRarityTargetSurvivesBeginValidation() {
        SpecificRarityCompetitionType type = new SpecificRarityCompetitionType();
        IRarity rarity = mock(IRarity.class);
        when(rarity.getId()).thenReturn("epic");
        FishManager fishManager = mock(FishManager.class);
        when(fishManager.getRarity("epic")).thenReturn(rarity);

        try (MockedStatic<FishManager> fishManagers = mockStatic(FishManager.class);
             MockedStatic<EvenMoreFish> plugin = mockPlugin()) {
            fishManagers.when(FishManager::getInstance).thenReturn(fishManager);
            Competition competition = new Competition(60, type);
            competition.setNumberNeeded(2);

            assertTrue(type.restoreResumeState(Map.of("selected-rarity", "epic"), competition));
            assertTrue(type.isUsable(competition));
            assertSame(rarity, type.getSelectedRarity());
            assertEquals(Map.of("selected-rarity", "epic"), type.saveResumeState());
            assertNotSame(type, type.freshInstance());
            assertFalse(type.freshInstance().restoreResumeState(Map.of("selected-rarity", "missing"), competition));
        }
    }

    @Test
    void backupCapturesStartTimeInMillisecondsWithoutChangingLegacySecondsGetter() {
        try (MockedStatic<EvenMoreFish> plugin = mockPlugin()) {
            Competition competition = new Competition(60, new LargestFishCompetitionType());
            Instant start = Instant.ofEpochMilli(1_766_000_000_123L);
            competition.setStartTime(start);
            competition.setNumberNeeded(3);

            CompetitionResumeSnapshot snapshot = competition.createResumeSnapshot();

            assertEquals(start.toEpochMilli(), snapshot.startTimeMillis());
            assertEquals(start.getEpochSecond(), competition.getEpochStartTime());
            assertEquals(3, snapshot.numberNeeded());
            assertTrue(snapshot.resumeSupported());
        }
    }

    private MockedStatic<EvenMoreFish> mockPlugin() {
        EvenMoreFish instance = mock(EvenMoreFish.class);
        when(instance.getDataFolder()).thenReturn(directory.toFile());
        MockedStatic<EvenMoreFish> plugin = mockStatic(EvenMoreFish.class);
        plugin.when(EvenMoreFish::getInstance).thenReturn(instance);
        return plugin;
    }
}
