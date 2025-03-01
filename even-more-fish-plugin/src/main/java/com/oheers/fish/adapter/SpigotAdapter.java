package com.oheers.fish.adapter;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.adapter.PlatformAdapter;
import com.oheers.fish.api.plugin.EMFPlugin;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.checkerframework.checker.units.qual.N;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class SpigotAdapter extends PlatformAdapter {

    private final NamespacedKey spawnerKey = new NamespacedKey(EvenMoreFish.getInstance(), "spawner-mob");

    public SpigotAdapter() {
        super();
    }

    @Override
    public void logLoadedMessage() {
        Logger logger = EMFPlugin.getInstance().getLogger();
        logger.info("Using API provided by Spigot.");
        logger.warning("Support for Spigot servers will be removed in the future in favour of Paper.");
        logger.warning("You can download Paper here: https://papermc.io/downloads/paper");
    }

    @Override
    public SpigotMessage createMessage(@NotNull String message) {
        return new SpigotMessage(message, this);
    }

    @Override
    public SpigotMessage createMessage(@NotNull List<String> messageList) {
        return new SpigotMessage(messageList, this);
    }

    @Override
    public ItemStack getSkullFromBase64(@NotNull String base64) {
        final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        UUID headUuid = UUID.randomUUID();
        // 1.20.5+ handling
        if (MinecraftVersion.isNewerThan(MinecraftVersion.MC1_20_R3)) {
            NBT.modifyComponents(
                skull, nbt -> {
                    ReadWriteNBT profileNbt = nbt.getOrCreateCompound("minecraft:profile");
                    profileNbt.setUUID("id", headUuid);
                    ReadWriteNBT propertiesNbt = profileNbt.getCompoundList("properties").addCompound();
                    // This key is required, so we set it to an empty string.
                    propertiesNbt.setString("name", "textures");
                    propertiesNbt.setString("value", base64);
                }
            );
        // 1.20.4 and below handling
        } else {
            NBT.modify(
                skull, nbt -> {
                    ReadWriteNBT skullOwnerCompound = nbt.getOrCreateCompound("SkullOwner");
                    skullOwnerCompound.setUUID("Id", headUuid);
                    skullOwnerCompound.getOrCreateCompound("Properties")
                        .getCompoundList("textures")
                        .addCompound()
                        .setString("Value", base64);
                }
            );
        }
        return skull;
    }

    @Override
    public ItemStack getSkullFromUUID(@NotNull UUID uuid) {
        final ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        // 1.20.5+ handling
        if (MinecraftVersion.isNewerThan(MinecraftVersion.MC1_20_R3)) {
            NBT.modifyComponents(
                skull, nbt -> {
                    ReadWriteNBT profileNbt = nbt.getOrCreateCompound("minecraft:profile");
                    profileNbt.setUUID("id", uuid);
                }
            );
        // 1.20.4 and below handling
        } else {
            NBT.modify(
                skull, nbt -> {
                    ReadWriteNBT skullOwnerCompound = nbt.getOrCreateCompound("SkullOwner");
                    skullOwnerCompound.setUUID("Id", uuid);
                }
            );
        }
        return skull;
    }

    @EventHandler
    public void onSpawnerSpawn(SpawnerSpawnEvent event) {
        if (event.isCancelled()) {
            return;
        }
        event.getEntity().getPersistentDataContainer().set(spawnerKey, PersistentDataType.STRING, "true");
    }

    @Override
    public boolean isSpawnerMob(@NotNull Entity entity) {
        return entity.getPersistentDataContainer().has(spawnerKey, PersistentDataType.STRING);
    }

}
