package com.oheers.fish.fishing;

import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.permissions.UserPerms;
import com.oheers.fish.utils.nbt.NbtKeys;
import com.oheers.fish.utils.nbt.NbtUtils;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;

public class FishingProcessor extends Processor<PlayerFishEvent> {

    @Override
    @EventHandler(priority = EventPriority.HIGHEST)
    public void process(PlayerFishEvent event) {
        if (!isCustomFishAllowed(event.getPlayer())) {
            return;
        }

        if (MainConfig.getInstance().requireNBTRod()) {
            //check if player is using the fishing rod with correct nbt value.
            ItemStack rodInHand = event.getPlayer().getInventory().getItemInMainHand();
            if (rodInHand.getType() != Material.AIR) {
                if (!NbtUtils.hasKey(rodInHand, NbtKeys.EMF_ROD_NBT)) {
                    //tag is null or tag is false
                    return;
                }
            }
        }

        if (MainConfig.getInstance().requireFishingPermission()) {
            //check if player have permission to fish emf fishes
            if (!event.getPlayer().hasPermission(UserPerms.USE_ROD)) {
                if (event.getState() == PlayerFishEvent.State.FISHING) {//send msg only when throw the lure
                    ConfigMessage.NO_PERMISSION_FISHING.getMessage().send(event.getPlayer());
                }
                return;
            }
        }

        if (event.getState() == PlayerFishEvent.State.CAUGHT_FISH) {
            ItemStack fish = getFish(event.getPlayer(), event.getHook().getLocation(), event.getPlayer().getInventory().getItemInMainHand());

            if (fish == null) {
                return;
            }

            if (!(event.getCaught() instanceof Item nonCustom)) {
                return;
            }

            if (MainConfig.getInstance().giveStraightToInventory() && isSpaceForNewFish(event.getPlayer().getInventory())) {
                FishUtils.giveItem(fish, event.getPlayer());
                nonCustom.remove();
            } else {
                // replaces the fishing item with a custom evenmorefish fish.
                if (fish.getType().isAir()) {
                    nonCustom.remove();
                } else {
                    nonCustom.setItemStack(fish);
                }
            }
        }
    }

    @Override
    protected boolean competitionOnlyCheck() {
        if (MainConfig.getInstance().isFishCatchOnlyInCompetition()) {
            return Competition.isActive();
        } else {
            return true;
        }
    }

}