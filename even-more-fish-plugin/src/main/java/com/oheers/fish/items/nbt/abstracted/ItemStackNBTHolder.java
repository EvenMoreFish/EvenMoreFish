package com.oheers.fish.items.nbt.abstracted;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public abstract class ItemStackNBTHolder extends NBTHolder<ItemStack> {

    public ItemStackNBTHolder(@NotNull ItemStack obj) {
        super(obj);
    }

    public ItemStackNBTHolder(@NotNull String raw) {
        super(EvenMoreFish.getInstance().getVersionProvider().deserializeItemStack(raw));
    }

}
