package com.oheers.fish.adapter;

import com.destroystokyo.paper.profile.PlayerProfile;
import com.destroystokyo.paper.profile.ProfileProperty;
import com.oheers.fish.api.adapter.PlatformAdapter;
import com.oheers.fish.api.plugin.EMFPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.UUID;

public class PaperAdapter extends PlatformAdapter {

    public PaperAdapter() {
        super();
    }

    @Override
    public void logLoadedMessage() {
        EMFPlugin.getInstance().getComponentLogger().info("Using improved API provided by Paper.");
    }

    @Override
    public PaperMessage createMessage(@NotNull String message) {
        return new PaperMessage(message, this);
    }

    @Override
    public PaperMessage createMessage(@NotNull List<String> messageList) {
        return new PaperMessage(messageList, this);
    }

    @Override
    public ItemStack getSkullFromBase64(@NotNull String base64) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        skull.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = Bukkit.createProfile(UUID.randomUUID(), "EMFSkull");
            profile.setProperty(new ProfileProperty("textures", base64));
            meta.setPlayerProfile(profile);
        });
        return skull;
    }

    @Override
    public ItemStack getSkullFromUUID(@NotNull UUID uuid) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        skull.editMeta(SkullMeta.class, meta -> {
            PlayerProfile profile = Bukkit.createProfile(uuid, "EMFSkull");
            meta.setPlayerProfile(profile);
        });
        return skull;
    }

    @Override
    public boolean isSpawnerMob(@NotNull Entity entity) {
        return entity.fromMobSpawner();
    }

}
