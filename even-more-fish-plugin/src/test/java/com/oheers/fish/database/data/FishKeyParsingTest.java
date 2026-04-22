package com.oheers.fish.database.data;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

class FishKeyParsingTest {

    @Test
    void fishRarityKeyRoundTripsFishNamesContainingDots() {
        FishRarityKey key = FishRarityKey.of("ocean.blue.tang", "rare");

        FishRarityKey parsed = FishRarityKey.from(key.toString());

        assertEquals(key, parsed);
    }

    @Test
    void userFishRarityKeyRoundTripsFishNamesContainingDots() {
        UserFishRarityKey key = UserFishRarityKey.of(42, "ocean.blue.tang", "rare");

        UserFishRarityKey parsed = UserFishRarityKey.from(key.toString());

        assertEquals(key, parsed);
    }
}
