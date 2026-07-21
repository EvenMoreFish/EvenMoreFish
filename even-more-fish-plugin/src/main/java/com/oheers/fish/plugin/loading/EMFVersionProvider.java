package com.oheers.fish.plugin.loading;

import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.items.configs.ItemConfigProvider;
import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

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

    // Item Things

    public abstract @NotNull ItemConfigProvider createItemConfigProvider(@NotNull Section section);

    // NBT Things

    public abstract @NotNull NBTHolder<ItemStack> createItemStackNbtHolder(@NotNull ItemStack item);

    public abstract @Nullable ItemStack deserializeItemStack(@NotNull String raw);

    public abstract @NotNull String serializeItemStack(@NotNull ItemStack item);

    @ApiStatus.Internal
    public abstract @NotNull ItemStack getSkullFromUUID(@NotNull UUID uuid);

    @ApiStatus.Internal
    public abstract @NotNull ItemStack getSkullFromBase64(@NotNull String base64);

}
