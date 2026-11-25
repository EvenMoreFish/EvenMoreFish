package com.oheers.fish.fishing.items;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.requirement.Requirement;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.utils.WeightedRandom;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.List;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.ToDoubleFunction;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anySet;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

class FishManagerRegionBoostTest {

    @Test
    void zeroFactorExcludesRarityAndPositiveFractionDoesNotDuplicateIt() {
        Setup setup = setup();

        MainConfig config = mock(MainConfig.class);
        when(config.getRegionBoost("exampleFishingRegion", "Common")).thenReturn(0.0D);
        when(config.getRegionBoost("exampleFishingRegion", "Legendary")).thenReturn(1.5D);

        try (MockedStatic<FishUtils> regions = mockStatic(FishUtils.class);
             MockedStatic<MainConfig> configs = mockStatic(MainConfig.class)) {
            regions.when(() -> FishUtils.getRegionName(setup.location())).thenReturn("exampleFishingRegion");
            configs.when(MainConfig::getInstance).thenReturn(config);

            List<IRarity> eligible = setup.manager().getAvailableRarities(setup.fisher(), Set.of(setup.common(), setup.legendary()), null,
                RequirementContext.empty());

            assertEquals(List.of(setup.legendary()), eligible);

            when(config.getRegionBoost("exampleFishingRegion", "Legendary")).thenReturn(0.0D);
            assertEquals(List.of(), setup.manager().getAvailableRarities(setup.fisher(),
                Set.of(setup.common(), setup.legendary()), null, RequirementContext.empty()));
        }
    }

    @Test
    void normalAndBaitPickerWeightsApplyFractionalAndWholeRegionFactors() {
        Setup setup = setup();
        when(setup.common().getWeight()).thenReturn(100.0D);
        when(setup.legendary().getWeight()).thenReturn(1.0D);
        MainConfig config = mock(MainConfig.class);
        when(config.getRegionBoost("exampleFishingRegion", "Common")).thenReturn(1.0D);

        try (MockedStatic<FishUtils> regions = mockStatic(FishUtils.class);
             MockedStatic<MainConfig> configs = mockStatic(MainConfig.class);
             MockedStatic<WeightedRandom> weighted = mockStatic(WeightedRandom.class)) {
            regions.when(() -> FishUtils.getRegionName(setup.location())).thenReturn("exampleFishingRegion");
            configs.when(MainConfig::getInstance).thenReturn(config);
            AtomicReference<ToDoubleFunction<IRarity>> normalWeight = new AtomicReference<>();
            AtomicReference<ToDoubleFunction<IRarity>> baitWeight = new AtomicReference<>();
            weighted.when(() -> WeightedRandom.pick(anyList(), any(), eq(1.0D), anySet(), any()))
                .thenAnswer(invocation -> {
                    normalWeight.set(invocation.getArgument(1));
                    return null;
                });
            weighted.when(() -> WeightedRandom.pick(anyList(), any(), any()))
                .thenAnswer(invocation -> {
                    baitWeight.set(invocation.getArgument(1));
                    return null;
                });

            for (double factor : new double[] {0.5D, 1.0D, 1.5D, 15.0D}) {
                when(config.getRegionBoost("exampleFishingRegion", "Legendary")).thenReturn(factor);
                assertNull(setup.manager().getRandomWeightedRarity(setup.fisher(), 1.0D, Set.of(),
                    Set.of(setup.common(), setup.legendary()), null, RequirementContext.empty()));
                assertEquals(factor, normalWeight.get().applyAsDouble(setup.legendary()));

                assertNull(setup.manager().getWeightedRarity(setup.fisher(),
                    Set.of(setup.common(), setup.legendary()), IRarity::getWeight, null,
                    RequirementContext.empty()));
                assertEquals(factor, baitWeight.get().applyAsDouble(setup.legendary()));
                assertEquals(100.0D, baitWeight.get().applyAsDouble(setup.common()));
            }
        }
    }

    private Setup setup() {
        FishManager manager = mock(FishManager.class, CALLS_REAL_METHODS);
        IRarity common = rarity("Common");
        IRarity legendary = rarity("Legendary");
        TreeMap<String, IRarity> rarities = new TreeMap<>();
        rarities.put("Common", common);
        rarities.put("Legendary", legendary);
        doReturn(rarities).when(manager).getItemMap();
        Player fisher = mock(Player.class);
        Location location = mock(Location.class);
        when(fisher.getLocation()).thenReturn(location);
        return new Setup(manager, common, legendary, fisher, location);
    }

    private record Setup(FishManager manager, IRarity common, IRarity legendary, Player fisher, Location location) {}

    private IRarity rarity(String id) {
        IRarity rarity = mock(IRarity.class);
        when(rarity.getId()).thenReturn(id);
        when(rarity.getRequirement()).thenReturn(new Requirement());
        return rarity;
    }
}
