package com.oheers.fish.config;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

class MainConfigRegionBoostTest {

    @Test
    void acceptsFiniteNonNegativeNumericFactorsWithoutTruncation() {
        for (double factor : new double[] {0.0D, 0.5D, 1.0D, 1.5D, 15.0D}) {
            assertEquals(factor, MainConfig.parseRegionBoost(factor));
            assertEquals(factor, MainConfig.parseRegionBoost(Double.toString(factor)));
        }
    }

    @Test
    void rejectsNegativeNonFiniteAndNonNumericFactors() {
        assertNull(MainConfig.parseRegionBoost(-0.5D));
        assertNull(MainConfig.parseRegionBoost(Double.NaN));
        assertNull(MainConfig.parseRegionBoost(Double.POSITIVE_INFINITY));
        assertNull(MainConfig.parseRegionBoost("half"));
        assertNull(MainConfig.parseRegionBoost(null));
    }
}
