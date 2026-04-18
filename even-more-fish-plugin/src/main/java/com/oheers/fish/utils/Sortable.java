package com.oheers.fish.utils;

import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Comparator;
import java.util.TreeSet;

public interface Sortable {

    double getWeight();

    @NotNull String getId();

    enum SortType {
        ALPHABETICAL(Comparator.comparing(Sortable::getId)),
        // First sort by weight, and then sort alphabetically as a fallback.
        WEIGHT(Comparator.comparing(Sortable::getWeight).reversed().thenComparing(Sortable::getId));

        private final Comparator<Sortable> comparator;

        SortType(Comparator<Sortable> comparator) {
            this.comparator = comparator;
        }

        public <T extends Sortable> TreeSet<T> sort(@NotNull Collection<T> collection) {
            TreeSet<T> set = new TreeSet<>(comparator);
            set.addAll(collection);
            return set;
        }

    }

}
