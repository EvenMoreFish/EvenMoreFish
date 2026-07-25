package com.oheers.fish.baits.model;

import com.oheers.fish.fishing.items.Rarity;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record RarityChance(
    @NonNull Rarity rarity,
    double baseWeight,
    double effectiveWeight,
    double chance,
    @NonNull WeightModifier modifier,
    @NonNull List<FishChance> fishChances
) {}
