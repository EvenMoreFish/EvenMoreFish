package com.oheers.fish.fishing;

import com.oheers.fish.Checks;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.Logging;
import com.oheers.fish.baits.BaitHandler;
import com.oheers.fish.baits.manager.BaitNBTManager;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.fishing.rods.CustomRod;
import com.oheers.fish.fishing.rods.RodManager;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.text.DecimalFormat;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.Random;
import java.util.Set;

public abstract class Processor<E extends Event> {

    // Used for formatting fish length.
    private final DecimalFormat decimalFormat = new DecimalFormat("#.0");
    private final Random random = new Random();

    protected abstract void process(@NotNull E event);

    protected abstract boolean isEnabled();

    public @Nullable ItemStack getCaughtItem(@NotNull Player player, @NotNull Location location, @NotNull ItemStack fishingRod) {
        // Check if fishing is allowed in this world.
        if (!FishUtils.checkWorld(location)) {
            return null;
        }
        // Check if fishing is allowed in this WorldGuard or RedProtect region.
        if (!FishUtils.checkRegion(location, MainConfig.getInstance().getAllowedRegions())) {
            return null;
        }
        // Check for mcMMO overfishing
        if (Checks.isMcMMOOverfishing(player, location)) {
            return null;
        }

        double baitCatchPercentage = MainConfig.getInstance().getBaitCatchPercentage();
        if (shouldCatchBait() && baitCatchPercentage > 0 && random.nextDouble() * 100 < baitCatchPercentage) {
            return getBaitItem(player);
        }

        CustomRod customRod = RodManager.getInstance().getRod(fishingRod);
        BaitHandler bait = getBaitFromRod(fishingRod, customRod);

        Fish fish = chooseFish(player, location, bait, customRod);
        if (fish == null) {
            return null;
        }
        if (bait != null) {
            bait.handleFish(player, fish, fishingRod);
        }

        fish.init();
        // Fire the fish event and check for cancellation.
        if (!fireEvent(fish, player)) {
            return null;
        }
        handleCaughtFish(player, location, fish);
        leaderboardCheck(fish, player, location);
        return fish.give();
    }

    private void handleCaughtFish(@NotNull Player player, @NotNull Location location, @NotNull Fish fish) {
        if (fish.hasFishRewards()) {
            fish.getFishRewards().forEach(fishReward -> fishReward.rewardPlayer(player, location));
        }

        EvenMoreFish.getInstance().getMetricsManager().incrementFishCaught(1);
        if (fish.isSilent()) {
            return;
        }
        String length = decimalFormat.format(fish.getLength());

        EMFMessage message = fish.getLength() == -1 ?
            getLengthlessCaughtMessage().getMessage() :
            getCaughtMessage().getMessage();

        message.setPlayer(player);
        message.setLength(length);

        fish.getDisplayName();
        message.setFishCaught(fish.getDisplayName());
        message.setRarity(fish.getRarity().getDisplayName());

        if (fish.getRarity().getAnnounce()) {
            broadcastFishMessage(message, player);
        } else {
            message.send(player);
        }
    }

    private @Nullable BaitHandler getBaitFromRod(@NotNull ItemStack rod, @Nullable CustomRod customRod) {
        if (customRod != null) {
            return null;
        }
        if (MainConfig.getInstance().getBaitCompetitionDisable() && Competition.isActive()) {
            return null;
        }
        return BaitNBTManager.isBaitedRod(rod) ? BaitNBTManager.randomBaitApplication(rod) : null;
    }

    private @Nullable ItemStack getBaitItem(@NotNull Player player) {
        Optional<BaitHandler> caughtBait = BaitNBTManager.randomBaitCatch();
        if (caughtBait.isEmpty()) {
            Logging.debug("Could not determine bait for player " + player.getName() + ". This is usually a bug.");
            return null;
        }

        final BaitHandler bait = caughtBait.get();

        EMFMessage message = ConfigMessage.BAIT_CAUGHT.getMessage();
        message.setBait(bait.format(bait.getId()));
        message.setPlayer(player);
        message.send(player);

        return bait.create(player);
    }

    /**
     * Chooses a fish for the player. randomWeightedRarity & getFish methods are used to
     * choose the random fish.
     *
     * @param player   The fisher catching the fish.
     * @param location The location of the fisher.
     * @param bait The bait being used, null if no bait.
     * @param customRod The custom rod being used, null if no custom rod.
     * @return A random fish.
     */
    private @Nullable Fish chooseFish(@NotNull Player player, @NotNull Location location, @Nullable BaitHandler bait, @Nullable CustomRod customRod) {
        // Check if the bait exists and a custom rod does not. Custom rods are not compatible with baits.
        if (bait != null && customRod == null) {
            return bait.chooseFish(player, location);
        }

        Rarity rarity = FishManager.getInstance().getRandomWeightedRarity(
            player,
            1,
            Set.of(),
            Set.copyOf(FishManager.getInstance().getRarityMap().values()),
            customRod
        );
        if (rarity == null) {
            Logging.error("Could not determine a fish rarity for " + player.getName());
            return null;
        }

        Fish fish = FishManager.getInstance().getFish(
            rarity,
            location,
            player,
            1,
            null,
            true,
            this,
            customRod
        );
        if (fish == null) {
            EvenMoreFish.getInstance().getLogger().severe("Could not determine a fish for " + player.getName());
            return null;
        }
        fish.setFisherman(player.getUniqueId());
        return fish;
    }

    protected abstract boolean fireEvent(@NotNull Fish fish, @NotNull Player player);

    protected abstract ConfigMessage getCaughtMessage();

    protected abstract ConfigMessage getLengthlessCaughtMessage();

    // Checks

    protected boolean isCustomFishAllowed(Player player) {
        return isEnabled() && MainConfig.getInstance().getEnabled() && (competitionOnlyCheck() || EvenMoreFish.getInstance().isRaritiesCompCheckExempt())
            && !EvenMoreFish.getInstance().getToggle().isCustomFishingDisabled(player);
    }

    /**
     * Checks if the player should get a fish, respecting the only-in-competition option in config.yml
     */
    protected abstract boolean competitionOnlyCheck();

    protected abstract boolean shouldCatchBait();

    public abstract boolean canUseFish(@NotNull Fish fish);

    /**
     * Checks if we need to update the competition leaderboard.
     */
    protected void leaderboardCheck(@NotNull Fish fish, @NotNull Player fisherman, @NotNull Location location) {
        final Competition active = Competition.getCurrentlyActive();
        if (active == null) {
            return;
        }

        List<World> competitionWorlds = active.getCompetitionFile().getRequiredWorlds();
        if (!competitionWorlds.isEmpty()) {
            final World world = location.getWorld();
            if (world == null || !competitionWorlds.contains(world)) {
                return;
            }
        }
        active.applyToLeaderboard(fish, fisherman);
    }

    // Message

    private void broadcastFishMessage(@NotNull EMFMessage message, @NotNull Player sourcePlayer) {
        if (message.isEmpty()) {
            return;
        }

        Competition activeComp = Competition.getCurrentlyActive();

        Collection<? extends Player> validPlayers = getValidPlayers(sourcePlayer, activeComp);
        List<String> playerNames = validPlayers.stream().map(Player::getName).toList();
        EvenMoreFish.getInstance().debug("Valid players: " + String.join(", ", playerNames));

        message.send(validPlayers);
    }

    private @NotNull Collection<? extends Player> getValidPlayers(@NotNull Player sourcePlayer, @Nullable Competition activeComp) {
        if (activeComp == null) {
            return Bukkit.getOnlinePlayers().stream().toList();
        }

        CompetitionFile activeCompetitionFile = activeComp.getCompetitionFile();

        // Combine checks for fishing rod and broadcast range, to avoid unnecessary filtering.
        if (activeCompetitionFile.shouldBroadcastOnlyRods() || activeCompetitionFile.getBroadcastRange() > -1) {
            return Bukkit.getOnlinePlayers().stream()
                .filter(player -> checkRodRequirement(activeCompetitionFile, player))
                .filter(player -> isWithinRange(activeCompetitionFile, sourcePlayer, player, activeCompetitionFile.getBroadcastRange()))
                .toList();
        }

        return Bukkit.getOnlinePlayers();
    }

    private boolean checkRodRequirement(@NotNull CompetitionFile competition, @NotNull Player player) {
        if (!competition.shouldBroadcastOnlyRods()) {
            return true;
        }
        Material rodMaterial = Material.FISHING_ROD;
        return player.getInventory().getItemInMainHand().getType().equals(rodMaterial)
            || player.getInventory().getItemInOffHand().getType().equals(rodMaterial);
    }

    private boolean isWithinRange(@NotNull CompetitionFile competition, @NotNull Player sourcePlayer, @NotNull Player targetPlayer, int rangeSquared) {
        if (competition.getBroadcastRange() <= 0) {
            return true;
        }
        return sourcePlayer.getWorld().equals(targetPlayer.getWorld())
            && sourcePlayer.getLocation().distanceSquared(targetPlayer.getLocation()) <= rangeSquared;
    }

}
