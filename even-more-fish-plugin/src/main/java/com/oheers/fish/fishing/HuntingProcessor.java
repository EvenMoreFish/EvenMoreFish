package com.oheers.fish.fishing;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Fish;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.SpawnerSpawnEvent;
import org.bukkit.event.player.PlayerFishEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;

public class HuntingProcessor extends Processor<EntityDeathEvent> {

    @Override
    @EventHandler(priority = EventPriority.HIGHEST)
    protected void process(EntityDeathEvent event) {

        if (!(event.getEntity() instanceof org.bukkit.entity.Fish fishEntity)) {
            return;
        }

        // If spawner fish can't be hunted and the fish is from a spawner
        if (MainConfig.getInstance().isFishHuntIgnoreSpawnerFish() && EvenMoreFish.getAdapter().isSpawnerMob(fishEntity)) {
            return;
        }

        final Player player = event.getEntity().getKiller();
        if (player == null) {
            return;
        }

        if (!isCustomFishAllowed(player)) {
            return;
        }

        if (MainConfig.getInstance().requireFishingPermission()) {
            ConfigMessage.NO_PERMISSION_FISHING.getMessage().send(player);
            return;
        }

        ItemStack fish = getFish(player, fishEntity.getLocation(), player.getInventory().getItemInMainHand());

        if (fish == null) {
            return;
        }

        event.getDrops().clear();
        if (MainConfig.getInstance().giveStraightToInventory() && isSpaceForNewFish(player.getInventory())) {
            FishUtils.giveItem(fish, player);
        } else {
            // replaces the fishing item with a custom evenmorefish fish.
            if (!fish.getType().isAir()) {
                event.getDrops().add(fish);
            }
        }
    }

    @Override
    protected boolean competitionOnlyCheck() {
        if (!MainConfig.getInstance().isFishHuntOnlyInCompetition()) {
            return true;
        }
        Competition active = Competition.getCurrentlyActive();
        if (active == null) {
            return false;
        }
        return active.getCompetitionFile().isAllowHunting();
    }

}
