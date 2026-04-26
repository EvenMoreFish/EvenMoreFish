package com.oheers.fish.baits.model;

import com.oheers.fish.fishing.items.Fish;
import org.jetbrains.annotations.NotNull;

public record FishChance(
    @NotNull Fish fish,
    double baseWeight,
    double effectiveWeight,
    double conditionalChance,
    double overallChance,
    @NotNull WeightModifier modifier
) {}
