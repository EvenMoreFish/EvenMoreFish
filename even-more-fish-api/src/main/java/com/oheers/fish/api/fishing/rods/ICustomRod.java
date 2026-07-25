package com.oheers.fish.api.fishing.rods;

import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NonNull;

import java.util.List;

public interface ICustomRod {

    @NonNull String getId();

    boolean isDisabled();

    /**
     * Fetches the ItemFactory for this rod.
     * NOTE: Creating an ItemStack from this factory will not add the necessary NBT to identify the rod. Use {@link #create()} instead.
     */
    // TODO add ItemFactory after moved to API module.
    //@NonNull ItemFactory getFactory();
    //}

    /**
     * Creates an ItemStack of this rod, with the necessary NBT to identify it.
     */
    @NonNull ItemStack create();

    @NonNull List<? extends IRarity> getAllowedRarities();

    @NonNull List<? extends IFish> getAllowedFish();

}
