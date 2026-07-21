package com.oheers.fish;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.plugin.EMFPlugin;
import com.oheers.fish.commands.AdminCommand;
import com.oheers.fish.commands.MainCommand;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.items.nbt.abstracted.NBTHolder;
import com.oheers.fish.nbt.ItemStackNBTHolder;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;
import com.oheers.fish.plugin.loading.EMFVersionProvider;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class EMFVersion extends EMFVersionProvider {

    public EMFVersion(@NotNull EMFPlugin plugin) {
        super(plugin);
    }

    @Override
    public void load() {
        if (!NBT.preloadApi()) {
            throw new RuntimeException("NBT-API wasn't initialized properly, disabling the plugin");
        }
    }

    @Override
    public void enable() {
        Logging.warn(
            "After the release of Minecraft 26.3, " +
                "EvenMoreFish will no longer support 1.20.x servers. " +
                "We recommend you update to a newer version to keep up to date with bug fixes and new features."
        );
    }

    @Override
    public void loadCommands() {
        CommandAPIBukkitConfig config = new CommandAPIBukkitConfig(plugin)
            .shouldHookPaperReload(true)
            .missingExecutorImplementationMessage("You are not able to use this command!");
        CommandAPI.onLoad(config);
    }

    @Override
    public void enableCommands() {
        CommandAPI.onEnable();
    }

    @Override
    public void registerCommands() {
        new MainCommand().getCommand().register(plugin);

        // Shortcut command for /emf admin
        if (!MainConfig.getInstance().isAdminShortcutCommandEnabled()) {
            return;
        }
        String adminShortcut = MainConfig.getInstance().getAdminShortcutCommandName();
        new AdminCommand(adminShortcut).getCommand().register(plugin);
    }

    @Override
    public void resendCommands() {
        Bukkit.getOnlinePlayers().forEach(CommandAPI::updateRequirements);
    }

    @Override
    public void disableCommands() {
        CommandAPI.onDisable();
    }

    @Override
    public @NotNull NBTHolder<ItemStack> createItemStackNbtHolder(@NotNull ItemStack item) {
        return new ItemStackNBTHolder(item);
    }

    @Nullable
    @Override
    public ItemStack deserializeItemStack(@NotNull String raw) {
        ItemStack item = NBT.itemStackFromNBT(NBT.parseNBT(raw));
        if (item == null) {
            Logging.warn("Failed to parse an ItemStack from raw NBT: " + raw);
            return null;
        }
        return item;
    }

    @NotNull
    @Override
    public String serializeItemStack(@NotNull ItemStack item) {
        return NBT.itemStackToNBT(item).toString();
    }

    @Override
    public @NotNull ItemStack getSkullFromUUID(@NotNull UUID uuid) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        skull.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = Bukkit.createProfile(uuid, null);
            meta.setPlayerProfile(profile);
        });
        if (MinecraftVersion.isAtLeastVersion(MinecraftVersion.MC1_21_R4)) {
            NBT.modifyComponents(skull, nbt -> {
                nbt.getOrCreateCompound("minecraft:tooltip_display")
                    .getStringList("hidden_components")
                    .add("minecraft:profile");
            });
        }
        return skull;
    }

    @NotNull
    @Override
    public ItemStack getSkullFromBase64(@NotNull String base64) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        skull.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = Bukkit.createProfile(FishUtils.B64_SKULL_UUID, null);
            profile.setProperty(new ProfileProperty("textures", base64));
            meta.setPlayerProfile(profile);
        });
        return skull;
    }

    // Ignored Methods

    @Override
    public void reload() {}

}
