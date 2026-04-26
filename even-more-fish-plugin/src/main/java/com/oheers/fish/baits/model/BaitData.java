package com.oheers.fish.baits.model;

import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.Rarity;

import java.util.List;
import java.util.Map;

public record BaitData(
        String id,
        String displayName,
        List<Rarity> rarities,
        List<Fish> fish,
        Map<Rarity, WeightModifier> rarityModifiers,
        Map<Fish, WeightModifier> fishModifiers,
        boolean disabled,
        boolean infinite,
        int maxApplications,
        int dropQuantity,
        double applicationWeight,
        double catchWeight,
        boolean canBeCaught,
        boolean disableUseAlert
) {}
