package com.oheers.fish.baits.model;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;

import java.util.List;
import java.util.Map;

public record BaitData(
        String id,
        String displayName,
        List<IRarity> rarities,
        List<IFish> fish,
        Map<IRarity, WeightModifier> rarityModifiers,
        Map<IFish, WeightModifier> fishModifiers,
        boolean disabled,
        boolean infinite,
        int maxBaits,
        int dropQuantity,
        double applicationWeight,
        double catchWeight,
        boolean canBeCaught,
        boolean disableUseAlert
) {}
