package com.oheers.fish.plugin.loading;

import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.UUID;

public abstract class EMFVersionProvider {

    protected final EMFPlugin plugin;

    public EMFVersionProvider(@NonNull EMFPlugin plugin) {
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

    public abstract @NonNull NBTHolder<ItemStack> createItemStackNbtHolder(@NonNull ItemStack item);

    public abstract @Nullable ItemStack deserializeItemStack(@NonNull String raw);

    public abstract @NonNull String serializeItemStack(@NonNull ItemStack item);

    @ApiStatus.Internal
    public abstract @NonNull ItemStack getSkullFromUUID(@NonNull UUID uuid);

    @ApiStatus.Internal
    public abstract @NonNull ItemStack getSkullFromBase64(@NonNull String base64);

}
