package com.oheers.fish.fishing.items;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class FishManagerCatchLimitTest {

    @Test
    void unlimitedCatchLimitAllowsAnyCaughtCount() {
        assertTrue(FishManager.hasRemainingCatches(-1, 100));
        assertTrue(FishManager.hasRemainingCatches(0, 100));
    }

    @Test
    void positiveCatchLimitAllowsCaughtCountsBelowLimitOnly() {
        assertTrue(FishManager.hasRemainingCatches(3, 2));
        assertFalse(FishManager.hasRemainingCatches(3, 3));
        assertFalse(FishManager.hasRemainingCatches(3, 4));
    }
}
