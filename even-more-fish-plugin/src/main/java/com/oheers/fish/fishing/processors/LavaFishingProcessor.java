package com.oheers.fish.fishing.processors;

import com.oheers.fish.Checks;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.events.EMFFishCaughtEvent;
import com.oheers.fish.api.fishing.CatchType;
import com.oheers.fish.api.fishing.FishingType;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.Processor;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.messages.ConfigMessage;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.evenmorefish.dimensionfishing.common.TrackedHook;
import org.evenmorefish.dimensionfishing.events.LavaFishCaughtEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.LocalDateTime;

public class LavaFishingProcessor extends Processor<LavaFishCaughtEvent> implements Listener {

    private final EvenMoreFish plugin = EvenMoreFish.getInstance();

    public LavaFishingProcessor() {
        super(FishingType.LAVA);
    }

    @Override
    @EventHandler(priority = EventPriority.HIGHEST)
    public void process(@NotNull LavaFishCaughtEvent event) {
        TrackedHook hook = event.getTrackedHook();
        Player player = hook.getPlayer();

        ItemStack rod = getRod(player);
        if (rod == null) {
            plugin.debug("Fishing blocked: could not find rod.");
            return;
        }

        if (!isCustomFishAllowed(player)) {
            plugin.debug("Fishing blocked: custom fish not allowed for player %s.".formatted(player.getName()));
            return;
        }

        if (!Checks.canUseRod(rod)) {
            plugin.debug("Fishing blocked: rod unusable (%s).".formatted(rod));
            return;
        }

        ItemStack fish = getCaughtItem(player, hook.getFishHook().getLocation(), rod);

        if (fish == null) {
            plugin.debug("Could not obtain fish.");
            return;
        }

        // As there is no item to replace, always give directly to the player.
        FishUtils.giveItem(fish, player);
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
        return new EMFFishCaughtEvent(fish, player, LocalDateTime.now()).callEvent();
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

    private @Nullable ItemStack getRod(@NotNull Player player) {
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