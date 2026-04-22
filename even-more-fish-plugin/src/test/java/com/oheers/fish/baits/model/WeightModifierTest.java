package com.oheers.fish.baits.model;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class WeightModifierTest {

    @Test
    void parsesBareNumbersAsAdditiveBoosts() {
        WeightModifier modifier = WeightModifier.parse(10);

        assertEquals(15.0D, modifier.apply(5.0D));
        assertEquals("+10", modifier.describe());
    }

    @Test
    void parsesArithmeticExpressions() {
        assertEquals(12.0D, WeightModifier.parse("+7").apply(5.0D));
        assertEquals(2.0D, WeightModifier.parse("-3").apply(5.0D));
        assertEquals(10.0D, WeightModifier.parse("*2").apply(5.0D));
        assertEquals(2.5D, WeightModifier.parse("/2").apply(5.0D));
    }

    @Test
    void clampsNegativeResultsToZero() {
        assertEquals(0.0D, WeightModifier.parse("-50").apply(5.0D));
    }

    @Test
    void rejectsInvalidExpressions() {
        assertThrows(IllegalArgumentException.class, () -> WeightModifier.parse("*abc"));
    }
}
