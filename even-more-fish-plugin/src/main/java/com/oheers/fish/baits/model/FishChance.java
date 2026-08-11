package com.oheers.fish.baits.model;

import com.oheers.fish.api.fishing.items.IFish;
import org.jspecify.annotations.NonNull;

public record FishChance(
    @NonNull IFish fish,
    double baseWeight,
    double effectiveWeight,
    double conditionalChance,
    double overallChance,
    @NonNull WeightModifier modifier
) {}
