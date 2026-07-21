package com.oheers.fish.baits;

import com.oheers.fish.baits.model.WeightModifier;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class BaitHandlerTest {

    @Test
    void consumesBaitWhenCaughtFishMatchesModifiedRarity() {
        Rarity legendary = mock(Rarity.class);
        Fish fish = mock(Fish.class);
        when(fish.getRarity()).thenReturn(legendary);

        boolean shouldConsume = BaitHandler.shouldConsumeBait(
            Map.of(legendary, WeightModifier.multiply(2.0D)),
            Map.of(),
            fish
        );

        assertTrue(shouldConsume);
    }

    @Test
    void consumesBaitWhenCaughtFishMatchesModifiedFish() {
        Rarity rarity = mock(Rarity.class);
        Fish fish = mock(Fish.class);
        when(fish.getRarity()).thenReturn(rarity);

        boolean shouldConsume = BaitHandler.shouldConsumeBait(
            Map.of(),
            Map.of(fish, WeightModifier.multiply(2.0D)),
            fish
        );

        assertTrue(shouldConsume);
    }

    @Test
    void doesNotConsumeBaitWhenCaughtFishDoesNotMatchModifiers() {
        Rarity caughtRarity = mock(Rarity.class);
        Rarity boostedRarity = mock(Rarity.class);
        Fish fish = mock(Fish.class);
        when(fish.getRarity()).thenReturn(caughtRarity);

        boolean shouldConsume = BaitHandler.shouldConsumeBait(
            Map.of(boostedRarity, WeightModifier.multiply(2.0D)),
            Map.of(),
            fish
        );

        assertFalse(shouldConsume);
    }

    @Test
    void readsDocumentedMaxBaitsConfigKey() throws IOException {
        YamlDocument config = yaml("""
            max-baits: 20
            """);

        assertEquals(20, BaitHandler.resolveMaxBaits(config));
    }

    @Test
    void defaultsMaxBaitsToUnlimitedWhenUnset() throws IOException {
        YamlDocument config = yaml("");

        assertEquals(-1, BaitHandler.resolveMaxBaits(config));
    }

    private static YamlDocument yaml(String content) throws IOException {
        return YamlDocument.create(new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)));
    }
}
