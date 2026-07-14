package com.oheers.fish.api.boost;

import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jetbrains.annotations.NotNull;
import org.mockito.Mockito;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

class RarityBoostRegistryTest {

    private final RarityBoostRegistry registry = RarityBoostRegistry.getInstance();
    private final Player fisher = Mockito.mock(Player.class);
    private final Location location = new Location(null, 0, 0, 0);

    @BeforeAll
    static void setUpPluginInstance() throws ReflectiveOperationException {
        setPluginInstance(Mockito.mock(EMFPlugin.class));
    }

    @AfterAll
    static void tearDownPluginInstance() throws ReflectiveOperationException {
        setPluginInstance(null);
    }

    /** Registering logs through the plugin singleton, which does not exist in unit tests. */
    private static void setPluginInstance(EMFPlugin plugin) throws ReflectiveOperationException {
        Field instance = EMFPlugin.class.getDeclaredField("instance");
        instance.setAccessible(true);
        instance.set(null, plugin);
    }

    @BeforeEach
    void clearRegistry() {
        registry.clear();
    }

    @Test
    void registerAndUnregister() {
        RarityWeightBoost boost = fixedBoost("BoostPlugin", 2.0);

        assertTrue(registry.register(boost));
        assertSame(boost, registry.get("BoostPlugin"));
        assertFalse(registry.isEmpty());

        assertFalse(registry.register(fixedBoost("BoostPlugin", 3.0)), "duplicate keys should not register");
        assertTrue(registry.register(fixedBoost("BoostPlugin", 3.0), true), "force should overwrite");

        assertTrue(registry.unregister("BoostPlugin"));
        assertFalse(registry.unregister("BoostPlugin"));
        assertNull(registry.get("BoostPlugin"));
        assertTrue(registry.isEmpty());
    }

    @Test
    void combinedMultiplierIsOneWithoutBoosts() {
        assertEquals(1.0, registry.combinedMultiplier(fisher, location, "common"));
    }

    @Test
    void combinedMultiplierMultipliesAllBoosts() {
        registry.register(fixedBoost("PluginA", 2.0));
        registry.register(fixedBoost("PluginB", 1.5));

        assertEquals(3.0, registry.combinedMultiplier(fisher, location, "legendary"));
    }

    @Test
    void combinedMultiplierPassesTheRarityId() {
        registry.register(new RarityWeightBoost() {
            @Override
            public @NotNull String getKey() {
                return "LegendaryOnly";
            }

            @Override
            public double weightMultiplier(@NotNull Player fisher, @NotNull Location location, @NotNull String rarityId) {
                return rarityId.equals("legendary") ? 4.0 : 1.0;
            }
        });

        assertEquals(4.0, registry.combinedMultiplier(fisher, location, "legendary"));
        assertEquals(1.0, registry.combinedMultiplier(fisher, location, "common"));
    }

    @Test
    void combinedMultiplierClampsNegativeResults() {
        registry.register(fixedBoost("Misbehaving", -5.0));

        assertEquals(0.0, registry.combinedMultiplier(fisher, location, "common"));
    }

    private static RarityWeightBoost fixedBoost(String key, double multiplier) {
        return new RarityWeightBoost() {
            @Override
            public @NotNull String getKey() {
                return key;
            }

            @Override
            public double weightMultiplier(@NotNull Player fisher, @NotNull Location location, @NotNull String rarityId) {
                return multiplier;
            }
        };
    }

}
