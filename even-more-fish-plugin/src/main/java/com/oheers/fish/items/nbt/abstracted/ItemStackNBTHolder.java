package com.oheers.fish.items.nbt.abstracted;

import com.oheers.fish.EvenMoreFish;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

public abstract class ItemStackNBTHolder extends NBTHolder<ItemStack> {

    public ItemStackNBTHolder(@NonNull ItemStack obj) {
        super(obj);
    }

    public ItemStackNBTHolder(@NonNull String raw) {
        super(EvenMoreFish.getInstance().getVersionProvider().deserializeItemStack(raw));
    }

}
