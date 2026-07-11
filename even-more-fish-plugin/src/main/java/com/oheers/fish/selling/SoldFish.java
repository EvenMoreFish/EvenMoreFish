package com.oheers.fish.selling;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

public class SoldFish {

    private final Fish fish;
    private final String name;
    private final String rarity;
    private int amount;
    
    private double totalValue;
    private final double length;

    /**
     * Create a SoldFish instance from an ItemStack
     * @param item The ItemStack representing the fish being sold
     * @throws IllegalArgumentException if the ItemStack does not represent a valid fish
     */
    public SoldFish(@NotNull ItemStack item) throws IllegalArgumentException {
        IFish iFish = FishManager.getInstance().getFish(item);
        if ((!(iFish instanceof Fish f))) {
            throw new IllegalArgumentException("Item is not a fish.");
        }
        Optional<Double> worth = WorthNBT.getValue(f);
        if (worth.isEmpty()) {
            throw new IllegalArgumentException("Fish has no worth.");
        }
        this.fish = f;
        this.name = f.getName();
        this.rarity = f.getRarity().getId();
        this.length = f.getLength();
        this.amount = item.getAmount();
        this.totalValue = worth.get() * this.amount;
    }
    
    public void setAmount(int amount) {
        this.amount = amount;
    }
    
    public void setTotalValue(double totalValue) {
        this.totalValue = totalValue;
    }
    
    public @NotNull String getName() {
        return name;
    }
    
    public int getAmount() {
        return amount;
    }
    
    public double getTotalValue() {
        return totalValue;
    }
    
    public @NotNull String getRarity() {
        return rarity;
    }
    
    public double getLength() {
        return length;
    }

    public @NotNull Fish getFish() {
        return fish;
    }

}
