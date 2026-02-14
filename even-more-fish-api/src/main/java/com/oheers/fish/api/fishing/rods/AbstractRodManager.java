package com.oheers.fish.api.fishing.rods;

import com.oheers.fish.api.AbstractFileBasedManager;
import com.oheers.fish.api.baits.AbstractBaitManager;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public abstract class AbstractRodManager<T extends ICustomRod> extends AbstractFileBasedManager<T> {

    private static AbstractRodManager<? extends ICustomRod> instance;

    protected AbstractRodManager() {
        super(AbstractBaitManager.getInstance());
        if (instance != null) {
            throw new IllegalStateException("RodManager has already been initialized!");
        }
        instance = this;
    }

    public static @NotNull AbstractRodManager<? extends ICustomRod> getInstance() {
        if (instance == null) {
            throw new IllegalStateException("RodManager has not been initialized yet!");
        }
        return instance;
    }

    public abstract @Nullable ICustomRod getRod(@NotNull ItemStack item);

    public abstract @Nullable ICustomRod getRod(@NotNull String rodId);

}
