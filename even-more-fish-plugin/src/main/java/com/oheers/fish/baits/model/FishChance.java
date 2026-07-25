package com.oheers.fish.baits.model;

import com.oheers.fish.fishing.items.Fish;
import org.jspecify.annotations.NonNull;

public record FishChance(
    @NonNull Fish fish,
    double baseWeight,
    double effectiveWeight,
    double conditionalChance,
    double overallChance,
    @NonNull WeightModifier modifier
) {}
