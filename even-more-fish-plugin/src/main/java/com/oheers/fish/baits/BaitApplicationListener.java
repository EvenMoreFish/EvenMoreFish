package com.oheers.fish.baits;

import com.oheers.fish.Checks;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.baits.manager.BaitManager;
import com.oheers.fish.baits.manager.BaitNBTManager;
import com.oheers.fish.baits.model.ApplicationResult;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.exceptions.MaxBaitReachedException;
import com.oheers.fish.exceptions.MaxBaitsReachedException;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.utils.nbt.NbtKeys;
import com.oheers.fish.utils.nbt.NbtUtils;
import com.oheers.fish.utils.nbt.NbtVersion;
import de.tr7zw.changeme.nbtapi.NBT;
import de.tr7zw.changeme.nbtapi.iface.ReadWriteNBT;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryAction;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.AnvilInventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

/**
 * Handles inventory interactions between fishing rods and bait items in the EvenMoreFish plugin.
 * Manages bait application to rods, including NBT data conversion, game mode checks, and protection
 * against unauthorized modifications (e.g., via anvils). Also handles bait limits and player feedback.
 */
public class BaitApplicationListener implements Listener {

    @EventHandler
    public void onClickEvent(InventoryClickEvent event) {
        ItemStack potentialFishingRod = event.getCurrentItem();
        ItemStack cursor = event.getCursor();

        // Check anvil protection first
        if (MainConfig.getInstance().shouldProtectBaitedRods() && anvilCheck(event)) {
            return;
        }

        // Check if we need to continue applying a bait
        if (!BaitNBTManager.isBaitObject(cursor) || potentialFishingRod == null || !(event.getClickedInventory() instanceof PlayerInventory)) {
            return;
        }

        // Silently return if no fishing rod is held
        if (!potentialFishingRod.getType().equals(Material.FISHING_ROD)) {
            return;
        }

        // Tell the player if the rod is invalid
        if (!Checks.canUseRod(potentialFishingRod)) {
            ConfigMessage.BAIT_INVALID_ROD.getMessage().send(event.getWhoClicked());
            return;
        }

        GameMode gameMode = event.getWhoClicked().getGameMode();

        if (!gameMode.equals(GameMode.SURVIVAL) && !gameMode.equals(GameMode.ADVENTURE)) {
            ConfigMessage.BAIT_WRONG_GAMEMODE.getMessage().send(event.getWhoClicked());
            return;
        }

        ApplicationResult result;
        BaitHandler bait = BaitManager.getInstance().getBait(BaitNBTManager.getBaitName(event.getCursor()));

        if (bait == null) {
            return;
        }

        NbtVersion nbtVersion = NbtVersion.getVersion(potentialFishingRod);
        if (nbtVersion != NbtVersion.COMPAT) {
            convertToCompatNbtItem(nbtVersion, potentialFishingRod);
        }

        try {
            if (event.getAction() == InventoryAction.MOVE_TO_OTHER_INVENTORY) {
                result = BaitNBTManager.applyBaitedRodNBT(potentialFishingRod, bait, event.getCursor().getAmount());
                EvenMoreFish.getInstance().getMetricsManager().incrementBaitsApplied(event.getCursor().getAmount());
            } else {
                result = BaitNBTManager.applyBaitedRodNBT(potentialFishingRod, bait, 1);
                EvenMoreFish.getInstance().getMetricsManager().incrementBaitsApplied(1);
            }

        } catch (MaxBaitsReachedException exception) {
            ConfigMessage.BAITS_MAXED.getMessage().send(event.getWhoClicked());
            result = exception.getRecoveryResult();
        } catch (MaxBaitReachedException exception) {
            result = exception.getRecoveryResult();
            EMFMessage message = ConfigMessage.BAITS_MAXED_ON_ROD.getMessage();
            message.setBait(bait);
            message.send(event.getWhoClicked());
        }

        ItemStack resultRod = result.fishingRod();
        if (resultRod == null || resultRod.isEmpty()) {
            return;
        }

        event.setCancelled(true);
        event.setCurrentItem(resultRod);

        int cursorModifier = result.cursorItemModifier();

        if (cursor.getAmount() - cursorModifier == 0) {
            event.getWhoClicked().setItemOnCursor(new ItemStack(Material.AIR));
        } else {
            cursor.setAmount(cursor.getAmount() + cursorModifier);
            event.getWhoClicked().setItemOnCursor(cursor);
        }
    }

    private void convertToCompatNbtItem(final NbtVersion nbtVersion, final ItemStack fishingRod) {
        final String appliedBaitString = NbtUtils.getString(fishingRod, NbtKeys.EMF_APPLIED_BAIT);

        if (nbtVersion == NbtVersion.LEGACY) {
            final String namespacedKey = NbtKeys.EMF_COMPOUND + ":" + NbtKeys.EMF_APPLIED_BAIT;
            NBT.modify(fishingRod,nbt -> {
                ReadWriteNBT compound = nbt.getCompound(NbtKeys.PUBLIC_BUKKIT_VALUES);
                if (compound != null) {
                    compound.removeKey(namespacedKey);
                }
            });

            if (NBT.get(fishingRod, nbt -> {
                return nbt.hasTag(namespacedKey);
            })) {
                NBT.modify(fishingRod, nbt -> {
                    nbt.removeKey(namespacedKey);
                    ReadWriteNBT compound = nbt.getCompound("display");
                    if (compound != null) {
                        compound.getStringList("Lore").clear();
                    }
                });
            }
        }

        if (nbtVersion == NbtVersion.NBTAPI) {
            NBT.modify(fishingRod, nbt -> {
                nbt.removeKey(NbtKeys.EMF_COMPOUND + ":" + NbtKeys.EMF_APPLIED_BAIT);
            });
        }

        NBT.modify(fishingRod, nbt -> {
            nbt.getOrCreateCompound(NbtKeys.EMF_COMPOUND).setString(NbtKeys.EMF_APPLIED_BAIT, appliedBaitString);
        });

    }

    private boolean anvilCheck(InventoryClickEvent event) {
        if (!(event.getClickedInventory() instanceof AnvilInventory inv) || !(event.getWhoClicked() instanceof Player player)) {
            return false;
        }
        if (event.getSlot() == 2 && BaitNBTManager.isBaitedRod(inv.getItem(1))) {
            event.setCancelled(true);
            player.closeInventory();
            ConfigMessage.BAIT_ROD_PROTECTION.getMessage().send(player);
            return true;
        }
        return false;
    }

}
