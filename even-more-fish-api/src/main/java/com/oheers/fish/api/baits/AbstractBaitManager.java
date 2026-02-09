package com.oheers.fish.api.baits;

import com.oheers.fish.api.AbstractFileBasedManager;
import com.oheers.fish.api.fishing.items.AbstractFishManager;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractBaitManager<T extends IBait> extends AbstractFileBasedManager<T> {

    private static AbstractBaitManager<? extends IBait> instance;

    protected AbstractBaitManager() {
        super(AbstractFishManager.getInstance());
        if (instance != null) {
            throw new IllegalStateException("BaitManager has already been initialized!");
        }
        instance = this;
    }

    public static @NotNull AbstractBaitManager<? extends IBait> getInstance() {
        if (instance == null) {
            throw new IllegalStateException("BaitManager has not been initialized yet!");
        }
        return instance;
    }

    public abstract @Nullable IBait getBait(@Nullable String baitName);

    public abstract @Nullable IBait getBait(@Nullable ItemStack itemStack);

    public abstract @Nullable IBait getBait(@Nullable Entity itemEntity);

    public abstract boolean isBait(@Nullable ItemStack itemStack);

    public abstract boolean isBait(@Nullable Entity itemEntity);

}
