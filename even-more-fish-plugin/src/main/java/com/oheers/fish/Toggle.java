package com.oheers.fish;

import com.oheers.fish.competition.Bar;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.permissions.UserPerms;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;

public class Toggle {

    private final EvenMoreFish plugin;
    private final NamespacedKey fishToggleKey;
    private final NamespacedKey bossBarToggleKey;
    private final NamespacedKey catchMessageToggleKey;

    public Toggle(@NotNull EvenMoreFish plugin) {
        this.plugin = plugin;
        this.fishToggleKey = new NamespacedKey(plugin, "fish-disabled");
        this.bossBarToggleKey = new NamespacedKey(plugin, "bossbar-disabled");
        this.catchMessageToggleKey = new NamespacedKey(plugin, "catch-message-disabled");
    }

    // Fish Toggle

    public void performFishToggle(@NotNull Player player) {
        if (!player.hasPermission(UserPerms.TOGGLE_FISHING)) {
            ConfigMessage.TOGGLE_FISHING_NO_PERMISSION.send(player);
            return;
        }
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        // If custom fishing is disabled
        if (isCustomFishingDisabled(player)) {
            // Set fish-disabled to false
            pdc.set(fishToggleKey, PersistentDataType.BOOLEAN, false);
            ConfigMessage.TOGGLE_FISHING_ON.send(player);
        } else {
            // Set fish-disabled to true
            pdc.set(fishToggleKey, PersistentDataType.BOOLEAN, true);
            ConfigMessage.TOGGLE_FISHING_OFF.send(player);
        }
    }


    public boolean isCustomFishingDisabled(@NotNull Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        return pdc.getOrDefault(fishToggleKey, PersistentDataType.BOOLEAN, false);
    }

    // Bossbar Toggle

    public void performBossBarToggle(@NotNull Player player) {
        if (!player.hasPermission(UserPerms.TOGGLE_BOSSBAR)) {
            ConfigMessage.TOGGLE_BOSSBAR_NO_PERMISSION.send(player);
            return;
        }
        PersistentDataContainer pdc = player.getPersistentDataContainer();

        Competition activeComp = Competition.getCurrentlyActive();
        Bar activeBar = activeComp == null ? null : activeComp.getStatusBar();

        // If custom fishing is disabled
        if (isBossBarDisabled(player)) {
            // Set fish-disabled to false
            pdc.set(bossBarToggleKey, PersistentDataType.BOOLEAN, false);
            ConfigMessage.TOGGLE_BOSSBAR_ON.send(player);
            if (activeBar != null) {
                activeBar.addPlayer(player);
            }
        } else {
            // Set fish-disabled to true
            pdc.set(bossBarToggleKey, PersistentDataType.BOOLEAN, true);
            ConfigMessage.TOGGLE_BOSSBAR_OFF.send(player);
            if (activeBar != null) {
                activeBar.removePlayer(player);
            }
        }
    }


    public boolean isBossBarDisabled(@NotNull Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        return pdc.getOrDefault(bossBarToggleKey, PersistentDataType.BOOLEAN, false);
    }

    // Catch Message Toggle

    public void performCatchMessageToggle(@NotNull Player player) {
        if (!player.hasPermission(UserPerms.TOGGLE_CATCH_MESSAGE)) {
            ConfigMessage.TOGGLE_CATCH_MESSAGE_NO_PERMISSION.send(player);
            return;
        }
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        // If catch message is disabled
        if (isCatchMessageDisabled(player)) {
            // Set key to false
            pdc.set(catchMessageToggleKey, PersistentDataType.BOOLEAN, false);
            ConfigMessage.TOGGLE_CATCH_MESSAGE_ON.send(player);
        } else {
            // Set key to true
            pdc.set(catchMessageToggleKey, PersistentDataType.BOOLEAN, true);
            ConfigMessage.TOGGLE_CATCH_MESSAGE_OFF.send(player);
        }
    }


    public boolean isCatchMessageDisabled(@NotNull Player player) {
        PersistentDataContainer pdc = player.getPersistentDataContainer();
        return pdc.getOrDefault(catchMessageToggleKey, PersistentDataType.BOOLEAN, false);
    }

}
