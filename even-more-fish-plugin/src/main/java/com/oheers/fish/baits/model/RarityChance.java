package com.oheers.fish.baits.model;

import com.oheers.fish.fishing.items.Rarity;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public record RarityChance(
    @NotNull Rarity rarity,
    double baseWeight,
    double effectiveWeight,
    double chance,
    @NotNull WeightModifier modifier,
    @NotNull List<FishChance> fishChances
) {}
