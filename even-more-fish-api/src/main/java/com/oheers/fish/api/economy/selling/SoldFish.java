package com.oheers.fish.api.economy.selling;

import com.oheers.fish.api.fishing.items.AbstractFishManager;
import com.oheers.fish.api.fishing.items.IFish;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class SoldFish {

    private final @NotNull IFish fish;
    private final @NotNull Player player;
    private final @NotNull int quantity;
    private double value;

    private SoldFish(@NotNull IFish fish, @NotNull Player player, int quantity, double value) {
        this.fish = fish;
        this.player = player;
        this.value = value * quantity;
        this.quantity = quantity;
    }

    public static @Nullable SoldFish get(@NotNull Player player, @Nullable ItemStack item) {
        if (item == null || item.isEmpty()) {
            return null;
        }
        IFish fish = AbstractFishManager.getInstance().getFish(item);
        if (fish == null) {
            return null;
        }
        int amount = item.getAmount();
        double setWorth = fish.getSetWorth();
        float length = fish.getLength();
        if (setWorth > 0) {
            return new SoldFish(fish, player, amount, setWorth );
        } else if (length > 0.0D) {
            double multiplier = fish.getWorthMultiplier();
            double worth = multiplier <= 0.0D ? -1D : multiplier * length;
            return new SoldFish(fish, player, amount, worth);
        } else {
            return null;
        }
    }

    /**
     * @return The fish that the player is selling.
     */
    public @NotNull IFish getFish() {
        return this.fish.createCopy();
    }

    /**
     * @return The player selling this fish.
     */
    public @NotNull Player getPlayer() {
        return this.player;
    }

    /**
     * @return The amount of fish sold. This is typically the size of the ItemStack.
     */
    public int getQuantity() {
        return this.quantity;
    }

    /**
     * @return The value of this fish.
     */
    public double getValue() {
        return this.value;
    }

    /**
     * Sets the value of this fish.
     * @param value The new value of the fish. If this is below 0, the fish will not be sold.
     */
    public void setValue(double value) {
        this.value = value;
    }

}
