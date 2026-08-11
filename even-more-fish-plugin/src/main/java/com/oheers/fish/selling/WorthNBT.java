package com.oheers.fish.selling;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.fishing.items.FishManager;
import org.bukkit.block.Skull;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.Optional;

public class WorthNBT {

    private WorthNBT() {
        throw new UnsupportedOperationException();
    }

    public static void setNBT(@NonNull ItemStack fishItem, @NonNull IFish fish) {
        FishManager.getInstance().setFishNbt(fishItem, fish);
    }

    public static void setNBT(@NonNull Skull skull, @NonNull IFish fish) {
        FishManager.getInstance().setFishNbt(skull, fish);
    }

    public static @NonNull Optional<Double> getValue(@NonNull IFish fish) {
        double setWorth = fish.getSetWorth();
        float length = fish.getLength();
        if (setWorth > 0) {
            return Optional.of(setWorth);
        } else if (length > 0.0D) {
            return getMultipliedValue(length, fish);
        } else {
            return Optional.empty();
        }
    }

    private static Optional<Double> getMultipliedValue(float length, @NonNull IFish fish) {
        double multiplier = fish.getWorthMultiplier();
        if (multiplier <= 0.0D) {
            return Optional.empty();
        }
        return Optional.of(multiplier * length);
    }

}
