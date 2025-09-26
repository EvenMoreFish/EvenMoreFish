package com.oheers.fish.fishing;

import com.oheers.fish.Checks;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.EMFFishEvent;
import com.oheers.fish.api.fishing.CatchType;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.permissions.UserPerms;
import org.bukkit.Material;
import org.bukkit.Tag;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

public class FishingProcessor extends Processor<PlayerFishEvent> {
    private final EvenMoreFish plugin = EvenMoreFish.getInstance();

    @Override
    @EventHandler(priority = EventPriority.HIGHEST)
    public void process(@NotNull PlayerFishEvent event) {
        if (event.isCancelled()) {
            plugin.debug("Fishing event was cancelled. Skipping handling.");
            return;
        }

        ItemStack rod = getRod(event);

        if (!isCustomFishAllowed(event.getPlayer())) {
            plugin.debug("Fishing blocked: custom fish not allowed for player %s.".formatted(event.getPlayer().getName()));
            return;
        }

        if (!Checks.canUseRod(rod)) {
            plugin.debug("Fishing blocked: rod unusable (%s).".formatted(rod));
            return;
        }

        if (MainConfig.getInstance().requireFishingPermission() && !event.getPlayer().hasPermission(UserPerms.USE_ROD)) {
            plugin.debug("Fishing blocked: permission required and player lacks it.");
            if (event.getState() == PlayerFishEvent.State.FISHING) {
                //send msg only when throw the lure
                ConfigMessage.NO_PERMISSION_FISHING.getMessage().send(event.getPlayer());
            }
            return;
        }

        if (event.getState() != PlayerFishEvent.State.CAUGHT_FISH) {
            plugin.debug("Ignoring event state: %s".formatted(event.getState()));
            return;
        }

        if (!(event.getCaught() instanceof Item nonCustom)) {
            plugin.debug("Caught entity is not an Item.");
            return;
        }

        if (MainConfig.getInstance().isFishCatchOverrideOnlyFish() && !Tag.ITEMS_FISHES.isTagged(nonCustom.getItemStack().getType())) {
            plugin.debug("Caught item is not a vanilla fish, and we have been told to skip non-fish. Skipping.");
            return;
        }

        ItemStack fish = getFish(event.getPlayer(), event.getHook().getLocation(), rod);

        if (fish == null) {
            plugin.debug("Could not obtain fish.");
            return;
        }

        if (MainConfig.getInstance().giveStraightToInventory() && isSpaceForNewFish(event.getPlayer().getInventory())) {
            FishUtils.giveItem(fish, event.getPlayer());
            nonCustom.remove();
            return;
        }
        // replaces the fishing item with a custom evenmorefish fish.
        if (fish.getType().isAir()) {
            nonCustom.remove();
            return;
        }

        nonCustom.setItemStack(fish);
    }

    @Override
    protected boolean isEnabled() {
        return MainConfig.getInstance().isCatchEnabled();
    }

    @Override
    protected boolean competitionOnlyCheck() {
        Competition active = Competition.getCurrentlyActive();

        if (active != null) {
            return active.getCompetitionFile().isAllowFishing();
        }

        return !MainConfig.getInstance().isFishCatchOnlyInCompetition();
    }


    @Override
    protected boolean fireEvent(@NotNull Fish fish, @NotNull Player player) {
        EMFFishEvent fishEvent = new EMFFishEvent(fish, player, LocalDateTime.now());
        return fishEvent.callEvent();
    }

    @Override
    protected ConfigMessage getCaughtMessage() {
        return ConfigMessage.FISH_CAUGHT;
    }

    @Override
    protected ConfigMessage getLengthlessCaughtMessage() {
        return ConfigMessage.FISH_LENGTHLESS_CAUGHT;
    }

    @Override
    protected boolean shouldCatchBait() {
        return true;
    }

    @Override
    public boolean canUseFish(@NotNull Fish fish) {
        return fish.getCatchType().equals(CatchType.CATCH)
                || fish.getCatchType().equals(CatchType.BOTH);
    }

    private @Nullable ItemStack getRod(@NotNull PlayerFishEvent event) {
        Player player = event.getPlayer();

        // Use getHand() only if the state is FISHING
        if (event.getState() == PlayerFishEvent.State.FISHING) {
            EquipmentSlot hand = event.getHand();
            if (hand != null) {
                return player.getInventory().getItem(hand);
            }
            return null; // Defensive fallback, though shouldn't happen if state is FISHING
        }

        // Fallback: check both hands for a rod
        ItemStack mainHand = player.getInventory().getItem(EquipmentSlot.HAND);
        if (mainHand.getType() == Material.FISHING_ROD) {
            return mainHand;
        }

        ItemStack offHand = player.getInventory().getItem(EquipmentSlot.OFF_HAND);
        if (offHand.getType() == Material.FISHING_ROD) {
            return offHand;
        }

        // No rod found
        return null;
    }


}