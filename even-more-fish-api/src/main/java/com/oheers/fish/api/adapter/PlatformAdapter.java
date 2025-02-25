package com.oheers.fish.api.adapter;

import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public abstract class PlatformAdapter {

    public PlatformAdapter() {
        logLoadedMessage();
    }

    public abstract void logLoadedMessage();

    /**
     * Translates the provided message into a legacy string.
     * @return The provided message as a legacy string.
     */
    public String translateColorCodes(@NotNull String message) {
        return createMessage(message).getLegacyMessage();
    }

    public abstract AbstractMessage createMessage(@NotNull String message);

    public abstract AbstractMessage createMessage(@NotNull List<String> messageList);

    public abstract ItemStack getSkullFromBase64(@NotNull String base64);

    public abstract ItemStack getSkullFromUUID(@NotNull UUID uuid);

    public ItemStack getSkullFromUUID(@NotNull String uuidStr) {
        UUID uuid;
        try {
            uuid = UUID.fromString(uuidStr);
        } catch (IllegalArgumentException e) {
            EMFPlugin.getInstance().getLogger().warning("Invalid UUID provided for skull: " + uuidStr);
            return null;
        }
        return getSkullFromUUID(uuid);
    }

}
