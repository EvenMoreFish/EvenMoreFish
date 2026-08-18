package com.oheers.fish.api.economy.selling;

import com.oheers.fish.api.fishing.items.AbstractFishManager;
import com.oheers.fish.api.fishing.items.IFish;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.LocalDateTime;

public class SoldFish {

    private final @NonNull IFish fish;
    private final @Nullable Player player;
    private final @NonNull LocalDateTime sellTime;

    private int quantity;
    private double value;

    private SoldFish(@NonNull IFish fish, @Nullable Player player, int quantity, double value, @NonNull LocalDateTime sellTime) {
        this.fish = fish;
        this.player = player;
        this.sellTime = sellTime;

        this.value = value;
        this.quantity = quantity;
    }

    public static @Nullable SoldFish get(@Nullable Player player, @Nullable ItemStack item) {
        if (item == null || item.isEmpty()) {
            return null;
        }
        IFish fish = AbstractFishManager.getInstance().getFish(item);
        if (fish == null) {
            return null;
        }
        int amount = item.getAmount();
        double setWorth = fish.getSetWorth();
        if (setWorth > 0) {
            return new SoldFish(fish, player, amount, setWorth, LocalDateTime.now());
        } else if (fish.isLengthless()) {
            return null;
        } else {
            double multiplier = fish.getWorthMultiplier();
            double worth = multiplier <= 0.0D ? -1D : multiplier * fish.getLength();
            return new SoldFish(fish, player, amount, worth, LocalDateTime.now());
        }
    }

    /**
     * @return The fish that the player is selling.
     */
    public @NonNull IFish getFish() {
        return this.fish.createCopy();
    }

    /**
     * @return The player selling this fish.
     */
    public @Nullable Player getPlayer() {
        return this.player;
    }

    /**
     * @return The amount of fish sold. This is typically the size of the ItemStack.
     */
    public int getQuantity() {
        return this.quantity;
    }

    /**
     * Sets the quantity of this fish.
     * @param quantity The new quantity of the fish. This must be above 0.
     */
    public void setQuantity(int quantity) {
        this.quantity = Math.min(1, quantity);
    }

    /**
     * @return The value of this fish after applying quantity.
     */
    public double getFinalValue() {
        return this.value * this.quantity;
    }

    /**
     * @return The raw value of this fish.
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

    /**
     * @return The time this fish was sold at.
     */
    public @NonNull LocalDateTime getSellTime() {
        return this.sellTime;
    }

}
