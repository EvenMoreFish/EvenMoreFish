package com.oheers.fish.fishing;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.EMFFishHuntEvent;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.permissions.UserPerms;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;

public class HuntingProcessor extends Processor<EntityDeathEvent> {

    @Override
    @EventHandler(priority = EventPriority.HIGHEST)
    protected void process(EntityDeathEvent event) {
        if (!(event.getEntity() instanceof org.bukkit.entity.Fish fishEntity)) {
            return;
        }

        // If spawner fish can't be hunted and the fish is from a spawner
        if (MainConfig.getInstance().isFishHuntIgnoreSpawnerFish() && fishEntity.fromMobSpawner()) {
            return;
        }

        final Player player = event.getEntity().getKiller();
        if (player == null) {
            return;
        }

        if (!isCustomFishAllowed(player)) {
            return;
        }

        if (MainConfig.getInstance().requireFishingPermission() && !player.hasPermission(UserPerms.USE_ROD)) {
            ConfigMessage.NO_PERMISSION_FISHING.getMessage().send(player);
            return;
        }

        //Event gets fired here
        ItemStack fish = getFish(player, fishEntity.getLocation(), player.getInventory().getItemInMainHand());

        if (fish == null || fish.getType().isAir()) {
            return;
        }

        event.getDrops().clear();
        if (MainConfig.getInstance().giveStraightToInventory() && isSpaceForNewFish(player.getInventory())) {
            FishUtils.giveItem(fish, player);
        } else {
            // replaces the fishing item with a custom evenmorefish fish.
            event.getDrops().add(fish);
        }
    }

    @Override
    protected boolean isEnabled() {
        return MainConfig.getInstance().isHuntEnabled();
    }

    @Override
    protected boolean competitionOnlyCheck() {
        Competition active = Competition.getCurrentlyActive();

        if (active != null) {
            return active.getCompetitionFile().isAllowHunting();
        }

        return !MainConfig.getInstance().isFishHuntOnlyInCompetition();
    }

    @Override
    protected boolean fireEvent(@NotNull Fish fish, @NotNull Player player) {
        EMFFishHuntEvent fishHuntEvent = new EMFFishHuntEvent(fish, player, LocalDateTime.now());
        return fishHuntEvent.callEvent();
    }

    @Override
    protected ConfigMessage getCaughtMessage() {
        return ConfigMessage.FISH_HUNTED;
    }

    @Override
    protected ConfigMessage getLengthlessCaughtMessage() {
        return ConfigMessage.FISH_LENGTHLESS_HUNTED;
    }

    @Override
    protected boolean shouldCatchBait() {
        return false;
    }

    @Override
    public boolean canUseFish(@NotNull Fish fish) {
        return fish.getCatchType() == CatchType.HUNT || fish.getCatchType() == CatchType.BOTH;
    }

}
