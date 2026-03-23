package com.oheers.fish.utils;

import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;
import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Unit tests for {@link WeightedRandom}, verifying correct behavior of weighted selection
 * under various conditions, including:
 * <ul>
 *     <li>Basic weighted selection</li>
 *     <li>Boosted weights</li>
 *     <li>Handling of empty and zero-weight lists</li>
 *     <li>Statistical distribution over multiple runs</li>
 *     <li>Edge cases like negative weights and single-item input</li>
 * </ul>
 */
class WeightedRandomTest {

    record TestItem(String name, double weight) {

        @Override
        public @NotNull String toString() {
            return name;
        }
    }

    @Test
    void testPick_withSimpleWeights() {
        List<TestItem> items = List.of(
            new TestItem("A", 1.0),
            new TestItem("B", 2.0),
            new TestItem("C", 3.0)
        );

        Random rng = new Random(42); // fixed seed
        TestItem result = WeightedRandom.pick(items, i -> i.weight, rng);

        assertNotNull(result);
        assertTrue(Set.of("A", "B", "C").contains(result.name));
    }

    @Test
    void testPick_withBoostedElement() {
        TestItem a = new TestItem("A", 1.0);
        TestItem b = new TestItem("B", 1.0);
        List<TestItem> items = List.of(a, b);
        Set<TestItem> boosted = Set.of(b);

        Random rng = new Random(0); // ensure b has a higher chance
        Map<String, AtomicInteger> count = new HashMap<>();
        count.put("A", new AtomicInteger(0));
        count.put("B", new AtomicInteger(0));

        for (int i = 0; i < 1000; i++) {
            TestItem result = WeightedRandom.pick(items, i2 -> i2.weight, 2.0, boosted, rng);
            count.get(result.name).incrementAndGet();
        }

        // B should win more often because of boost
        assertTrue(count.get("B").get() > count.get("A").get());
    }

    @Test
    void testPick_returnsNullForEmptyList() {
        List<TestItem> items = Collections.emptyList();
        TestItem result = WeightedRandom.pick(items, i -> i.weight, new Random());
        assertNull(result);
    }

    @Test
    void testPick_withZeroWeights() {
        List<TestItem> items = List.of(
            new TestItem("A", 0.0),
            new TestItem("B", 0.0),
            new TestItem("C", 0.0)
        );

        // Should still return something — the last fallback handles this
        TestItem result = WeightedRandom.pick(items, i -> i.weight, new Random(0));
        assertNotNull(result);
        assertTrue(Set.of("A", "B", "C").contains(result.name));
    }

    @Test
    void testPick_distributionAccuracy() {
        // Verifies empirical frequency roughly matches theoretical ratio (1:2:3)
        // With weights 1:2:3, we expect approx 1/6, 2/6, 3/6 frequency
        List<TestItem> items = List.of(
            new TestItem("A", 1.0),
            new TestItem("B", 2.0),
            new TestItem("C", 3.0)
        );

        Random rng = new Random(123);
        Map<String, AtomicInteger> resultMap = new HashMap<>();
        resultMap.put("A", new AtomicInteger());
        resultMap.put("B", new AtomicInteger());
        resultMap.put("C", new AtomicInteger());

        int runs = 6000;
        for (int i = 0; i < runs; i++) {
            TestItem result = WeightedRandom.pick(items, i2 -> i2.weight, rng);
            resultMap.get(result.name).incrementAndGet();
        }

        int a = resultMap.get("A").get();
        int b = resultMap.get("B").get();
        int c = resultMap.get("C").get();

        // Expect roughly 1000 : 2000 : 3000 distribution
        assertTrue(a < b && b < c, "Expected order A < B < C in selection frequency");
    }

    @Test
    void testPick_withBoostRateDisabled() {
        TestItem a = new TestItem("A", 1.0);
        TestItem b = new TestItem("B", 1.0);
        List<TestItem> items = List.of(a, b);
        Set<TestItem> boosted = Set.of(b);

        Random rng = new Random(0);
        Map<String, AtomicInteger> count = new HashMap<>();
        count.put("A", new AtomicInteger());
        count.put("B", new AtomicInteger());

        for (int i = 0; i < 1000; i++) {
            TestItem result = WeightedRandom.pick(items, i2 -> i2.weight, -1.0, boosted, rng);
            count.get(result.name).incrementAndGet();
        }

        // Should be roughly equal since boost is disabled
        int delta = Math.abs(count.get("A").get() - count.get("B").get());
        assertTrue(delta < 150, "Expected near-equal distribution when boost is disabled");
    }

    @Test
    void testPick_withSingleItem() {
        TestItem only = new TestItem("Solo", 5.0);
        List<TestItem> items = List.of(only);

        for (int i = 0; i < 100; i++) {
            TestItem result = WeightedRandom.pick(items, i2 -> i2.weight, new Random(i));
            assertEquals("Solo", result.name);
        }
    }

    @Test
    void testPick_withNegativeWeights() {
        List<TestItem> items = List.of(
            new TestItem("A", -10.0),
            new TestItem("B", 0.0),
            new TestItem("C", 5.0)
        );

        Random rng = new Random(42);
        TestItem result = WeightedRandom.pick(items, i -> i.weight, rng);
        assertEquals("C", result.name);
    }

}
