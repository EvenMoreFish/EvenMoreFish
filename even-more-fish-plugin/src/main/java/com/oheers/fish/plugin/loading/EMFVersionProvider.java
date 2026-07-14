package com.oheers.fish.plugin.loading;

import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.items.nbt.NBTHolder;
import org.bukkit.block.BlockState;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public abstract class EMFVersionProvider {

    protected final EMFPlugin plugin;

    public EMFVersionProvider(@NotNull EMFPlugin plugin) {
        this.plugin = plugin;
    }

    public abstract void load();

    public abstract void enable();

    public abstract void reload();

    /**
     * Stuff to do onLoad() with commands
     */
    public abstract void loadCommands();

    public abstract void enableCommands();

    public abstract void registerCommands();

    public abstract void resendCommands();

    public abstract void disableCommands();

    // NBT Things

    public abstract @NotNull NBTHolder<ItemStack> createItemStackNbtHolder(@NotNull ItemStack item);

    public abstract @NotNull NBTHolder<BlockState> createBlockStateNbtHolder(@NotNull BlockState state);

    @ApiStatus.Internal
    public abstract @NotNull ItemStack getSkullFromUUID(@NotNull UUID uuid);

    @ApiStatus.Internal
    public abstract @NotNull ItemStack getSkullFromBase64(@NotNull String base64);

}
