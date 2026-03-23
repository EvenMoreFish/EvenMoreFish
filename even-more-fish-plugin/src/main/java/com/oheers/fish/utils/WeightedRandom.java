package com.oheers.fish.utils;

import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ThreadLocalRandom;
import java.util.function.ToDoubleFunction;

public final class WeightedRandom {
    private WeightedRandom() {
        throw new UnsupportedOperationException();
    }

    /**
     * Selects a random element from the given list using weighted random selection.
     * <p>
     * Each element is assigned a weight using the provided {@code weightFunction}.
     * Elements with higher weights have a proportionally higher chance of being selected.
     * <p>
     * This version does not apply any boosting to specific elements and uses a boost rate of {@code 1.0}.
     *
     * @param elements       the list of elements to pick from
     * @param weightFunction a function that provides the weight for each element (must return non-negative values)
     * @param random         an optional {@link Random} instance to use; if {@code null}, a thread-local random will be used
     * @param <T>            the type of elements in the list
     * @return a randomly selected element based on weights, or {@code null} if the list is empty
     */
    public static <T> T pick(
            List<T> elements,
            ToDoubleFunction<T> weightFunction,
            Random random
    ) {
        return pick(elements, weightFunction, 1.0, Collections.emptySet(), random);
    }

    /**
     * Selects a random element from the given list using weighted random selection, with optional weight boosting.
     * <p>
     * Each element is assigned a weight using the provided {@code weightFunction}. Elements included in the {@code boosted}
     * set will have their weight multiplied by {@code boostRate}, unless {@code boostRate} is {@code -1}, which disables boosting.
     * <p>
     * Elements with higher effective weights have a proportionally higher chance of being selected.
     * If the total effective weight of all elements is zero or negative, a uniformly random element is selected instead.
     *
     * @param elements       the list of elements to pick from; must not be {@code null}
     * @param weightFunction a function that provides the base weight for each element (should return non-negative values)
     * @param boostRate      the multiplier to apply to elements in the {@code boosted} set; set to {@code -1} to disable boosting
     * @param boosted        a set of elements whose weights should be boosted if {@code boostRate} is not {@code -1}
     * @param random         an optional {@link Random} instance to use; if {@code null}, a thread-local random will be used
     * @param <T>            the type of elements in the list
     * @return a randomly selected element based on weighted probability, or {@code null} if the list is empty
     */

    public static <T> @Nullable T pick(
            @NotNull List<T> elements,
            ToDoubleFunction<T> weightFunction,
            double boostRate,
            Set<T> boosted,
            Random random
    ) {
        if (elements.isEmpty()) {
            return null;
        }

        Random rng = (random != null) ? random : ThreadLocalRandom.current();
        double totalWeight = calcTotalWeight(elements, weightFunction, boostRate, boosted);

        if (totalWeight <= 0) {
            return elements.get(rng.nextInt(elements.size()));
        }

        double r = rng.nextDouble() * totalWeight;
        for (T element : elements) {
            double weight = getEffectiveWeight(element, weightFunction, boostRate, boosted);
            r -= weight;
            if (r <= 0.0) return element;
        }

        // Should never reach here
        return elements.getLast();
    }

    /**
     * Calculates the total effective weight of a list of elements using a provided weight function,
     * with optional boosting applied to elements in the {@code boosted} set.
     * <p>
     * Elements with non-positive effective weight contribute nothing to the total.
     *
     * @param elements       the list of elements whose total weight is being calculated
     * @param weightFunction the function that provides base weights for the elements
     * @param boostRate      the multiplier applied to elements in the {@code boosted} set; {@code -1} disables boosting
     * @param boosted        a set of elements to boost, if boosting is enabled
     * @param <T>            the type of the elements
     * @return the sum of effective weights of all elements
     */
    private static <T> double calcTotalWeight(
            @NotNull List<T> elements,
            ToDoubleFunction<T> weightFunction,
            double boostRate,
            Set<T> boosted
    ) {
        double total = 0.0;
        for (T element : elements) {
            total += getEffectiveWeight(element, weightFunction, boostRate, boosted);
        }
        return total;
    }


    /**
     * Calculates the effective weight of an element by applying the base weight function
     * and optionally boosting it if the element is in the {@code boosted} set.
     * <p>
     * If the base weight is less than or equal to zero, returns {@code 0.0}.
     * If {@code boostRate} is {@code -1}, boosting is disabled.
     *
     * @param element        the element whose weight is being calculated
     * @param weightFunction the function used to get the base weight
     * @param boostRate      the multiplier for boosted elements; {@code -1} disables boosting
     * @param boosted        a set of elements to which the boostRate should be applied
     * @param <T>            the type of the element
     * @return the effective weight of the element, or {@code 0.0} if the base weight is non-positive
     */
    private static <T> double getEffectiveWeight(
            T element,
            @NotNull ToDoubleFunction<T> weightFunction,
            double boostRate,
            Set<T> boosted
    ) {
        double baseWeight = weightFunction.applyAsDouble(element);
        if (baseWeight <= 0.0) return 0.0;

        return (boostRate != -1 && boosted.contains(element)) ? baseWeight * boostRate : baseWeight;
    }

}
