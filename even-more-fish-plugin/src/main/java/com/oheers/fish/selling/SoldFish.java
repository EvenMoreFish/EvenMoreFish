package com.oheers.fish.selling;

import com.oheers.fish.fishing.items.Fish;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

/**
 * @deprecated Use {@link com.oheers.fish.api.economy.selling.SoldFish} instead.
 */
@Deprecated(forRemoval = true)
public class SoldFish {

    private final com.oheers.fish.api.economy.selling.SoldFish ref;

    /**
     * Create a SoldFish instance from an ItemStack
     * @param item The ItemStack representing the fish being sold
     * @throws IllegalArgumentException if the ItemStack does not represent a valid fish
     */
    public SoldFish(@NotNull ItemStack item) throws IllegalArgumentException {
        com.oheers.fish.api.economy.selling.SoldFish ref = com.oheers.fish.api.economy.selling.SoldFish.get(null, item);
        if (ref == null) {
            throw new IllegalArgumentException("Item is not a sellable fish.");
        }
        this.ref = ref;
    }

    public SoldFish(@NotNull com.oheers.fish.api.economy.selling.SoldFish ref) {
        this.ref = ref;
    }

    public void setAmount(int amount) {
        ref.setQuantity(amount);
    }
    
    public void setTotalValue(double totalValue) {
        ref.setValue(totalValue);
    }
    
    public @NotNull String getName() {
        return ref.getFish().getName();
    }
    
    public int getAmount() {
        return ref.getQuantity();
    }
    
    public double getTotalValue() {
        return ref.getFinalValue();
    }
    
    public @NotNull String getRarity() {
        return ref.getFish().getRarity().getId();
    }
    
    public double getLength() {
        return ref.getFish().getLength();
    }

    public @NotNull Fish getFish() {
        if (!(ref.getFish() instanceof Fish fish)) {
            throw new IllegalArgumentException("SoldFish is not an internal Fish object.");
        }
        return fish;
    }

}
