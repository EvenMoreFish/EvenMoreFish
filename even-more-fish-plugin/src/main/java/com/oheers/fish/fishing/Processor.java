package com.oheers.fish.fishing;

import com.gmail.nossr50.config.experience.ExperienceConfig;
import com.gmail.nossr50.util.player.UserManager;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.EMFFishEvent;
import com.oheers.fish.api.adapter.AbstractMessage;
import com.oheers.fish.baits.Bait;
import com.oheers.fish.baits.BaitNBTManager;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.config.BaitFile;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.messages.ConfigMessage;
import com.oheers.fish.database.DataManager;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.checkerframework.framework.qual.InheritedAnnotation;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.lang.annotation.Inherited;
import java.text.DecimalFormat;
import java.util.List;
import java.util.Set;

public abstract class Processor<E extends Event> implements Listener {

    protected abstract void process(E event);

    private final DecimalFormat decimalFormat = new DecimalFormat("#.0");

    protected boolean isSpaceForNewFish(Inventory inventory) {
        for (ItemStack item : inventory.getContents()) {
            if (item == null) {
                return true;
            }
        }
        return false;
    }

    protected boolean isCustomFishAllowed(Player player) {
        return MainConfig.getInstance().getEnabled() && (competitionOnlyCheck() || EvenMoreFish.getInstance().isRaritiesCompCheckExempt()) && EvenMoreFish.getInstance().isCustomFishing(player);
    }

    /**
     * Chooses a bait without needing to specify a bait to be used. randomWeightedRarity & getFish methods are used to
     * choose the random fish.
     *
     * @param player   The fisher catching the fish.
     * @param location The location of the fisher.
     *                 {@code @returns} A random fish without any bait application.
     */
    protected Fish chooseFish(@NotNull Player player, @NotNull Location location, @Nullable Bait bait) {
        if (bait != null) {
            return bait.chooseFish(player, location);
        }
        Rarity fishRarity = FishManager.getInstance().getRandomWeightedRarity(player, 1, null, Set.copyOf(FishManager.getInstance().getRarityMap().values()));
        if (fishRarity == null) {
            EvenMoreFish.getInstance().getLogger().severe("Could not determine a rarity for fish for " + player.getName());
            return null;
        }

        Fish fish = FishManager.getInstance().getFish(fishRarity, location, player, 1, null, true);
        if (fish == null) {
            EvenMoreFish.getInstance().getLogger().severe("Could not determine a fish for " + player.getName());
            return null;
        }
        fish.setFisherman(player.getUniqueId());
        return fish;
    }

    protected ItemStack getFish(@NotNull Player player, @NotNull Location location, @NotNull ItemStack fishingRod) {
        if (!FishUtils.checkRegion(location, MainConfig.getInstance().getAllowedRegions())) {
            return null;
        }
        if (!FishUtils.checkWorld(location)) {
            return null;
        }

        if (EvenMoreFish.getInstance().isUsingMcMMO()
            && ExperienceConfig.getInstance().isFishingExploitingPrevented()
            && UserManager.getPlayer(player).getFishingManager().isExploitingFishing(location.toVector())) {
            return null;
        }

        if (BaitFile.getInstance().getBaitCatchPercentage() > 0
            && EvenMoreFish.getInstance().getRandom().nextDouble() * 100.0 < BaitFile.getInstance().getBaitCatchPercentage()) {
            Bait caughtBait = BaitNBTManager.randomBaitCatch();
            if (caughtBait != null) {
                AbstractMessage message = ConfigMessage.BAIT_CAUGHT.getMessage();
                message.setBaitTheme(caughtBait.getTheme());
                message.setBait(caughtBait.getName());
                message.setPlayer(player);
                message.send(player);

                return caughtBait.create(player);
            }
        }

        Bait applyingBait = null;

        if (BaitNBTManager.isBaitedRod(fishingRod) && (!BaitFile.getInstance().competitionsBlockBaits() || !Competition.isActive())) {
            applyingBait = BaitNBTManager.randomBaitApplication(fishingRod);
        }

        Fish fish = chooseFish(player, location, applyingBait);

        if (fish == null) {
            return null;
        }

        if (applyingBait != null) {
            applyingBait.handleFish(player, fish, fishingRod);
        }

        fish.init();

        EMFFishEvent cEvent = new EMFFishEvent(fish, player);
        Bukkit.getPluginManager().callEvent(cEvent);
        if (cEvent.isCancelled()) return null;

        fish.checkFishEvent();

        if (fish.hasFishRewards()) {
            fish.getFishRewards().forEach(fishReward -> fishReward.rewardPlayer(player, location));
        }

        if (!fish.isSilent()) {
            String length = decimalFormat.format(fish.getLength());
            String rarity = FishUtils.translateColorCodes(fish.getRarity().getId());

            AbstractMessage message = ConfigMessage.FISH_CAUGHT.getMessage();
            message.setPlayer(player);
            message.setRarityColour(fish.getRarity().getColour());
            message.setRarity(rarity);
            message.setLength(length);

            EvenMoreFish.getInstance().incrementMetricFishCaught(1);

            fish.getDisplayName();
            message.setFishCaught(fish.getDisplayName());
            message.setRarity(fish.getRarity().getDisplayName());


            if (fish.getLength() == -1) {
                message.setMessage(ConfigMessage.FISH_LENGTHLESS_CAUGHT.getMessage());
            }

            if (fish.getRarity().getAnnounce()) {
                FishUtils.broadcastFishMessage(message, player, false);
            } else {
                message.send(player);
            }
        }

        competitionCheck(fish, player, location);

        if (MainConfig.getInstance().isDatabaseOnline()) {
            EvenMoreFish.getScheduler().runTaskAsynchronously(() -> {
                if (EvenMoreFish.getInstance().getDatabase().hasFishData(fish)) {
                    EvenMoreFish.getInstance().getDatabase().incrementFish(fish);

                    if (EvenMoreFish.getInstance().getDatabase().getLargestFishSize(fish) < fish.getLength()) {
                        EvenMoreFish.getInstance().getDatabase().updateLargestFish(fish, player.getUniqueId());
                    }
                } else {
                    EvenMoreFish.getInstance().getDatabase().createFishData(fish, player.getUniqueId());
                }

                DataManager.getInstance().handleFishCatch(player.getUniqueId(), fish);
            });
        }

        return fish.give(-1);
    }

    // Checks if it should be giving the player the fish considering the fish-only-in-competition option in config.yml
    protected abstract boolean competitionOnlyCheck();

    protected void competitionCheck(Fish fish, Player fisherman, Location location) {
        Competition active = Competition.getCurrentlyActive();
        if (active == null) {
            return;
        }
        List<World> competitionWorlds = active.getCompetitionFile().getRequiredWorlds();
        if (!competitionWorlds.isEmpty()) {
            if (location.getWorld() != null) {
                if (!competitionWorlds.contains(location.getWorld())) {
                    return;
                }
            }
        }
        active.applyToLeaderboard(fish, fisherman);
    }

}
