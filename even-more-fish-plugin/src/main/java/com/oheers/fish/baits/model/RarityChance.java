package com.oheers.fish.baits.model;

import com.oheers.fish.api.fishing.items.IRarity;
import org.jspecify.annotations.NonNull;

import java.util.List;

public record RarityChance(
    @NonNull IRarity rarity,
    double baseWeight,
    double effectiveWeight,
    double chance,
    @NonNull WeightModifier modifier,
    @NonNull List<FishChance> fishChances
) {}
