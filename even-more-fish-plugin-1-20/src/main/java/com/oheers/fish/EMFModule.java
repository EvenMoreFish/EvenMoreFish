package com.oheers.fish;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.oheers.fish.commands.AdminCommand;
import com.oheers.fish.commands.MainCommand;
import com.oheers.fish.config.MainConfig;
import dev.jorel.commandapi.CommandAPI;
import dev.jorel.commandapi.CommandAPIBukkitConfig;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

public class EMFModule extends EvenMoreFish {
    @Override
    public void loadCommands() {
        CommandAPIBukkitConfig config = new CommandAPIBukkitConfig(this)
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
        new MainCommand().getCommand().register(this);

        // Shortcut command for /emf admin
        if (!MainConfig.getInstance().isAdminShortcutCommandEnabled()) {
            return;
        }
        String adminShortcut = MainConfig.getInstance().getAdminShortcutCommandName();
        new AdminCommand(adminShortcut).getCommand().register(this);
    }

    @Override
    public void resendCommands() {
        Bukkit.getOnlinePlayers().forEach(CommandAPI::updateRequirements);
    }

    @Override
    public void disableCommands() {
        CommandAPI.onDisable();
    }

    // Can probably be moved somewhere else, but they're here for now.

    @NotNull
    @Override
    public ItemStack getSkullFromUUID(@NotNull UUID uuid) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        skull.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = Bukkit.createProfile(uuid, null);
            meta.setPlayerProfile(profile);
        });
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
}
