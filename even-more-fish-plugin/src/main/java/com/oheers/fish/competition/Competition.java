package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.EMFCompetitionEndEvent;
import com.oheers.fish.api.EMFCompetitionStartEvent;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.config.ConfigBase;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.api.fishing.items.IRarity;
import com.oheers.fish.api.requirement.RequirementContext;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.api.utils.Scheduling;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.competition.timer.CompetitionBackupTimer;
import com.oheers.fish.competition.timer.CompetitionTimer;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.MessageConfig;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.database.model.CompetitionReport;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import dev.dejvokep.boostedyaml.YamlDocument;
import net.kyori.adventure.sound.Sound;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.ApiStatus;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Competition {

    private static final CompetitionManager manager = CompetitionManager.getInstance();

    protected Leaderboard leaderboard;
    private @Nullable CompetitionType competitionType;
    private IFish selectedFish;
    private IRarity selectedRarity;
    private String competitionName;
    protected boolean adminStarted = false;
    private EMFMessage startMessage;
    protected long maxDuration;
    protected long timeLeft;
    private CompetitionBossbar statusBar;
    private Long epochStartTime;
    private LocalDateTime startTime;
    private final List<Long> alertTimes;
    private final Map<Integer, List<Reward>> rewards;
    private int playersNeeded;
    private Sound startSound;
    private CompetitionTimer timingSystem;
    private CompetitionBackupTimer backupSystem;
    private CompetitionFile competitionFile;
    private int numberNeeded = 0;
    private UUID singleWinner = null;

    public Competition(final @NonNull CompetitionFile competitionFile) {
        this.competitionFile = competitionFile;
        this.competitionName = competitionFile.getId();
        this.playersNeeded = competitionFile.getPlayersNeeded();
        this.startSound = competitionFile.getStartSound();
        this.maxDuration = competitionFile.getDuration() * 60L;
        this.timeLeft = this.maxDuration;
        this.alertTimes = competitionFile.getAlertTimes();
        this.rewards = competitionFile.getRewards();
        this.numberNeeded = competitionFile.getNumberNeeded();

        // Resolve CompetitionType last as it may depend on some values set above.
        this.competitionType = resolveType(competitionFile, this);
    }

    private static @Nullable CompetitionType resolveType(@NonNull CompetitionFile file, @NonNull Competition competition) {
        CompetitionType type = file.getType();
        if (type == null) {
            Logging.warn("Invalid competition: " + file.getId() + " has an invalid competition type.");
            return null;
        }
        if (type instanceof CompetitionType.Random random) {
            return new CompetitionType.Forwarding(random, random.getRandomType(competition));
        }
        return type;
    }

    /**
     * @return A valid bossbar for this competition. Null if it should not be shown.
     */
    private @NonNull CompetitionBossbar createBossbar() {
        CompetitionBossbar bar = new CompetitionBossbar();
        bar.setShouldShow(competitionFile.shouldShowBossbar());
        bar.setColour(competitionFile.getBossbarColour());

        EMFSingleMessage prefix = competitionFile.getBossbarPrefix();
        if (selectedRarity != null) {
            prefix.setRarity(selectedRarity.getDisplayName());
        } else if (selectedFish != null) {
            prefix.setRarity(selectedFish.getRarity().getDisplayName());
            prefix.setVariable("{fish}", selectedFish.getDisplayName());
        }
        if (competitionType != null) {
            bar.setPrefix(prefix, competitionType);
        }
        return bar;
    }

    public Competition(final long duration, final @NonNull CompetitionType type) {
        this.maxDuration = duration;
        this.alertTimes = new ArrayList<>();
        this.rewards = new HashMap<>();
        this.competitionType = type;
    }

    /**
     * Sets the maximum duration of the competition in seconds.
     * @param durationSeconds The maximum duration of the competition in seconds.
     */
    public void setMaxDuration(long durationSeconds) {
        this.maxDuration = durationSeconds;
    }

    /**
     * Sets the time left in the competition in seconds.
     * @param durationSeconds The time left of the competition in seconds.
     */
    public void setTimeLeft(long durationSeconds) {
        this.timeLeft = durationSeconds;
    }

    /**
     * Combines {@link #setMaxDuration(long)} and {@link #setTimeLeft(long)}.
     * @param durationSeconds The time left of the competition in seconds.
     */
    public void setTime(long durationSeconds) {
        setMaxDuration(durationSeconds);
        setTimeLeft(durationSeconds);
    }

    /**
     * Adds more time to this competition.
     * @param durationSeconds The duration to add in seconds.
     */
    public void addTime(long durationSeconds) {
        this.maxDuration += durationSeconds;
        this.timeLeft += durationSeconds;
    }

    public boolean isPlayerRequirementMet() {
        return EvenMoreFish.getInstance().getVisibleOnlinePlayers().size() >= playersNeeded;
    }

    public boolean begin() {
        // Don't start a comp with no duration.
        if (maxDuration <= 0) {
            Logging.warn("Tried to start a competition with an invalid duration: " + competitionFile.getId());
            return false;
        }
        if (timeLeft <= 0) {
            Logging.warn("Tried to start a competition that has already ended.");
            return false;
        }
        try {
            if (!isAdminStarted() && !isPlayerRequirementMet()) {
                ConfigMessage.NOT_ENOUGH_PLAYERS.getMessage().broadcast();
                return false;
            }

            if (competitionType == null) {
                Logging.warn("Cannot start competition " + competitionName + ": Invalid CompetitionType.");
                return false;
            }

            if (!competitionType.isUsable(this)) {
                return false;
            }

            // Sets the active competition to this one. If another competition is active, it will be ended.
            manager.setActive(this);

            this.leaderboard = new Leaderboard(competitionType);

            if (this.statusBar == null) {
                this.statusBar = createBossbar();
            }
            statusBar.show();

            initTimer();
            announceBegin();
            EMFCompetitionStartEvent startEvent = new EMFCompetitionStartEvent(this);
            Bukkit.getServer().getPluginManager().callEvent(startEvent);

            if (epochStartTime == null) {
                setStartTime(Instant.now());
            }

            // Execute start commands
            getCompetitionFile().getStartCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));
            return true;
        } catch (Exception ex) {
            Logging.error("An exception was thrown while starting the competition.", ex);
            end(true);
            return false;
        }
    }

    public void end(boolean startFail) {
        end(startFail, false);
    }

    public void end(boolean startFail, boolean save) {
        // Print leaderboard
        if (timingSystem != null) {
            timingSystem.stop();
        }
        if (backupSystem != null) {
            backupSystem.stop();
        }
        if (statusBar != null) {
            statusBar.hide();
        }

        if (startFail) {
            manager.activeCompetition = null;
            return;
        }

        if (save && MainConfig.getInstance().shouldCompetitionResume()) {
            saveToFile();
            return;
        }

        try {
            // Delete the backup file in case it still exists for whatever reason.
            CompetitionManager.dataFile.delete();
            fireEndEvent();
            notifyPlayers();
            processRewards();
            resetCompetitionTypeIfForwarding();
            updateDatabase();
            leaderboard.clear();
        } catch (Exception exception) {
            EvenMoreFish.getInstance().getLogger().log(
                Level.SEVERE,
                "An exception was thrown while the competition was being ended!",
                exception
            );
        } finally {
            // Always set timeLeft to 0.
            this.timeLeft = 0;
            manager.activeCompetition = null;
            manager.checkHeldCompetition();
        }
    }

    private void fireEndEvent() {
        EMFCompetitionEndEvent endEvent = new EMFCompetitionEndEvent(this);
        endEvent.callEvent();
    }

    private void notifyPlayers() {
        for (Player player : Bukkit.getOnlinePlayers()) {
            ConfigMessage.COMPETITION_END.getMessage().send(player);
            sendLeaderboard(player);
        }
    }

    private void processRewards() {
        if (competitionType == null) {
            return;
        }
        if (competitionType.isSingleReward()) {
            if (singleWinner == null) {
                Logging.warn("Single-winner competition ended without a winner.");
                return;
            }
            singleReward(singleWinner);
        } else {
            handleRewards();
        }
    }

    private void resetCompetitionTypeIfForwarding() {
        if (competitionType instanceof CompetitionType.Forwarding forwarding) {
            competitionType = forwarding.getRandom();
        }
    }

    private void updateDatabase() {
        if (!DatabaseUtil.isDatabaseOnline()) {
            return;
        }

        EvenMoreFish plugin = EvenMoreFish.getInstance();
        plugin.getPluginDataManager().getCompetitionDataManager().update(
                competitionName,
                new CompetitionReport(this, startTime, LocalDateTime.now())
        );

    }

    // Starts a TimerTask to decrease the time left by 1s each second
    private void initTimer() {
        CompetitionTimer timer = new CompetitionTimer(this);
        timer.start();
        this.timingSystem = timer;

        // Also init the backup timer if enabled.
        if (MainConfig.getInstance().isCompetitionBackupEnabled()) {
            CompetitionBackupTimer backupTimer = new CompetitionBackupTimer(this);
            backupTimer.start();
            this.backupSystem = backupTimer;
        }
    }

    /**
     * Checks for scheduled alerts and whether the competition should end for each second - this is called automatically
     * by the competition ticker every 20 ticks.
     *
     * @param timeLeft How many seconds are left for the competition.
     * @return true if the competition is ending, false if not.
     */
    private boolean processCompetitionSecond(long timeLeft) {
        if (alertTimes.contains(timeLeft)) {
            EMFMessage message = format(ConfigMessage.TIME_ALERT);
            message.broadcast();
        } else if (timeLeft <= 0) {
            end(false);
            return true;
        }
        return false;
    }

    public @NonNull EMFMessage format(@NonNull ConfigMessage configMessage) {
        return format(configMessage.getMessage());
    }

    public @NonNull EMFMessage format(@NonNull Component message) {
        return format(EMFSingleMessage.of(message));
    }

    public @NonNull EMFMessage format(@NonNull EMFMessage message) {
        if (competitionType == null) {
            Logging.warn("Cannot format a Competition message: Invalid CompetitionType.");
            return EMFSingleMessage.empty();
        }
        message.setTimeFormatted(FishUtils.timeFormat(timeLeft));
        message.setTimeRaw(FishUtils.timeRaw(timeLeft));
        message.setCompetitionType(competitionType.getTypeVariable());

        if (numberNeeded <= 0) {
            return message;
        }

        message.setAmount(numberNeeded);
        // Specific Rarity
        if (selectedRarity != null) {
            message.setRarity(selectedRarity);
            return message;
        }
        if (selectedFish != null) {
            message.setRarity(selectedFish.getRarity());
            message.setFishCaught(selectedFish);
        }
        return message;
    }

    public boolean decreaseTime() {
        if (processCompetitionSecond(timeLeft)) {
            return true;
        }
        timeLeft--;
        return false;
    }

    public void applyToLeaderboard(IFish fish, Player fisher) {
        UUID uuid = fisher.getUniqueId();
        // Ensure this is executed on the global scheduler to avoid CMEs.
        Scheduling.getInstance().runTask(() -> {
            if (competitionType == null) {
                return;
            }
            competitionType.applyToLeaderboard(fish, uuid, leaderboard, this);
        });
    }

    public void announceBegin() {
        getStartMessage().broadcast();
        if (startSound != null) {
            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(startSound, Sound.Emitter.self()));
        }
    }

    public void sendLeaderboard(@NonNull CommandSender sender) {
        if (!manager.isCompetitionActive()) {
            ConfigMessage.NO_COMPETITION_RUNNING.getMessage().send(sender);
            return;
        }
        if (leaderboard.getSize() == 0) {
            ConfigMessage.NO_FISH_CAUGHT.getMessage().send(sender);
            return;
        }

        List<String> competitionColours = competitionFile.getLeaderboardColours();
        List<CompetitionEntry> entries = leaderboard.getEntries();

        EMFMessage leaderboardMessage = buildLeaderboardMessage(entries, competitionColours);
        leaderboardMessage.send(sender);

        EMFMessage message = ConfigMessage.LEADERBOARD_TOTAL_PLAYERS.getMessage();
        message.setAmount(Integer.toString(leaderboard.getSize()));
        message.send(sender);
    }

    private @NonNull EMFListMessage buildLeaderboardMessage(List<CompetitionEntry> entries, List<String> competitionColours) {
        if (competitionType == null) {
            Logging.warn("Cannot fetch leaderboard message: Invalid CompetitionType.");
            return EMFListMessage.empty();
        }
        if (entries == null) {
            entries = List.of();
        }

        int maxCount = MessageConfig.getInstance().getLeaderboardCount();

        List<Component> leaderboard = new ArrayList<>();
        int pos = 0;

        for (CompetitionEntry entry : entries) {
            pos++;
            // If we're out of colours or the max count is reached, break the loop
            if (pos > competitionColours.size() || pos > maxCount) {
                break;
            }

            // Get the leaderboard message with length/amount defined
            EMFSingleMessage message = EMFSingleMessage.of(competitionType.formatLeaderboardEntry(entry));

            // Format remaining variables
            OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getPlayer());

            String name = FishUtils.getPlayerNameOrDefault(player, "Unknown Player");
            EMFSingleMessage colour = EMFSingleMessage.fromString(competitionColours.get(pos - 1));
            colour.setVariable("{name}", name);

            // {pos_colour} to empty, {player} to new colour
            message.setVariable("{pos_colour}", "");
            message.setVariable("{player}", colour);
            message.setPlayer(player);

            message.setPosition(Integer.toString(pos));
            message.setRarity(entry.getFish().getRarity().getDisplayName());
            message.setFishCaught(entry.getFish().getDisplayName());

            leaderboard.add(message.getComponentMessage());
        }

        return EMFListMessage.ofList(leaderboard);
    }

    private void handleDatabaseUpdates(CompetitionEntry entry, boolean isTopEntry) {
        if (!DatabaseUtil.isDatabaseOnline()) {
            return;
        }

        UserReport userReport = EvenMoreFish.getInstance().getPluginDataManager().getUserReportDataManager().get(String.valueOf(entry.getPlayer()));
        if (userReport == null) {
            EvenMoreFish.getInstance().getLogger().severe("Could not fetch User Report for " + entry.getPlayer() + ", their data has not been modified.");
            return;
        }

        if (isTopEntry) {
            userReport.incrementCompetitionsWon(1);
        }

        userReport.incrementCompetitionsJoined(1);
    }

    private void handleRewards() {
        if (leaderboard.getSize() == 0) {
            ConfigMessage.NO_WINNERS.getMessage().broadcast();
            return;
        }

        int rewardPlace = 1;

        List<CompetitionEntry> entries = leaderboard.getEntries();

        CompetitionEntry topEntry = leaderboard.getTopEntry();
        if (DatabaseUtil.isDatabaseOnline() && !entries.isEmpty()) {
            handleDatabaseUpdates(topEntry, true); // Top entry
        }

        for (CompetitionEntry entry : entries) {
            OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getPlayer());

            // Does the player's place have reward?
            if (rewards.containsKey(rewardPlace)) {
                rewards.get(rewardPlace).forEach(reward -> reward.give(player, null));
            } else {
                // Default to participation reward if not.
                List<Reward> participation = rewards.get(-1);
                if (participation != null) {
                    participation.forEach(reward -> reward.give(player, null));
                }
            }

            // Checking if the top entry is this one, as we handle database for the top entry above.
            if (!topEntry.getPlayer().equals(entry.getPlayer())) {
                handleDatabaseUpdates(entry, false);
            }

            // Increment the place
            rewardPlace++;
        }
    }

    private void singleReward(UUID winner) {
        if (competitionType == null) {
            return;
        }
        OfflinePlayer player = Bukkit.getOfflinePlayer(winner);

        EMFMessage message = format(ConfigMessage.COMPETITION_SINGLE_WINNER);
        message.setPlayer(player);
        message.setCompetitionType(competitionType.getTypeVariable());

        message.broadcast();

        if (!rewards.isEmpty()) {
            for (Reward reward : rewards.get(1)) {
                reward.give(player, null);
            }
        }

        // Handle database updates for all entries. This was originally missed.

        CompetitionEntry entry = leaderboard.getEntry(player.getUniqueId());
        handleDatabaseUpdates(entry, true);

        leaderboard.getEntries().forEach(e -> {
            if (e.getPlayer().equals(player.getUniqueId())) {
                return;
            }
            handleDatabaseUpdates(e, false);
        });
    }

    public @NonNull CompetitionBossbar getStatusBar() {
        return this.statusBar;
    }

    public @Nullable CompetitionType getCompetitionType() {
        return competitionType;
    }

    public void setNumberNeeded(int numberNeeded) {
        this.numberNeeded = numberNeeded;
    }

    public int getLeaderboardSize() {
        return leaderboard.getSize();
    }

    public @NonNull Leaderboard getLeaderboard() {
        return leaderboard;
    }

    public @NonNull EMFMessage getStartMessage() {
        if (competitionType == null) {
            Logging.warn("Cannot fetch competition start message: Invalid CompetitionType.");
            return EMFSingleMessage.empty();
        }
        if (startMessage == null) {
            startMessage = ConfigMessage.COMPETITION_START.getMessage();
            startMessage.setCompetitionType(competitionType.getTypeVariable());
        }
        return startMessage;
    }

    public @NonNull String getCompetitionName() {
        return competitionName;
    }

    public @Nullable LocalDateTime getStartTime() {
        return startTime;
    }

    public void setStartTime(@NonNull Instant instant) {
        this.epochStartTime = instant.getEpochSecond();
        this.startTime = LocalDateTime.ofInstant(instant, ZoneId.systemDefault());
    }

    public @Nullable Long getEpochStartTime() {
        return epochStartTime;
    }

    public @NonNull CompetitionFile getCompetitionFile() {
        return this.competitionFile;
    }

    public void setCompetitionType(@Nullable CompetitionType competitionType) {
        this.competitionType = competitionType;
    }

    public @Nullable IFish getSelectedFish() {
        return selectedFish;
    }

    public @Nullable IRarity getSelectedRarity() {
        return selectedRarity;
    }

    public int getNumberNeeded() {
        return numberNeeded;
    }

    public boolean isAdminStarted() {
        return adminStarted;
    }

    public void setAdminStarted(boolean adminStarted) {
        this.adminStarted = adminStarted;
    }

    public long getTimeLeft() {
        return this.timeLeft;
    }

    /**
     * @return The configured max duration.
     */
    public long getMaxDuration() {
        return this.maxDuration;
    }

    public boolean chooseFish() {
        List<IRarity> configRarities = getAllowedRaritiesOrLog();
        if (configRarities == null) return false;

        final Logger logger = EvenMoreFish.getInstance().getLogger();

        List<IFish> fishPool = new ArrayList<>();
        for (IRarity rarity : configRarities) {
            fishPool.addAll(rarity.getOriginalFishList());
        }

        if (fishPool.isEmpty()) {
            logger.severe("No fish available in allowed rarities for " + getCompetitionName());
            return false;
        }

        try {
            IFish selectedFish = FishManager.getInstance().getRandomWeightedFish(fishPool, 1.0d, null);
            if (selectedFish == null) {
                throw new IllegalArgumentException("No fish selected from pool");
            }

            this.selectedFish = selectedFish;
            return true;

        } catch (Exception e) {
            logger.severe(() -> "Could not load: " + getCompetitionName() + " because a random fish could not be chosen.");
            logger.severe(() -> "fishPool.size(): " + fishPool.size());
            logger.severe(() -> "configRarities.size(): " + configRarities.size());
            logger.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
    }


    public boolean chooseRarity() {
        List<IRarity> configRarities = getAllowedRaritiesOrLog();
        if (configRarities == null) return false;

        final Logger logger = EvenMoreFish.getInstance().getLogger();

        try {
            IRarity rarity = configRarities.get(EvenMoreFish.RANDOM.nextInt(configRarities.size()));

            if (rarity == null) {
                rarity = FishManager.getInstance().getRandomWeightedRarity(
                    null,
                    0,
                    Collections.emptySet(),
                    Set.copyOf(FishManager.getInstance().getRarityMap().values()),
                    null,
                    // RequirementContext cannot be filled as we have nothing to base it on.
                    RequirementContext.empty()
                );
            }

            if (rarity == null) {
                logger.severe("No rarity could be chosen for " + getCompetitionName());
                return false;
            }

            this.selectedRarity = rarity;
            return true;

        } catch (Exception e) {
            logger.severe("Could not load: " + getCompetitionName() + " because a random rarity could not be chosen.");
            logger.severe(() -> "rarityMap.size(): " + FishManager.getInstance().getRarityMap().size());
            logger.severe(() -> "configRarities.size(): " + configRarities.size());
            logger.log(Level.SEVERE, e.getMessage(), e);
            return false;
        }
    }

    public void setSingleWinner(@Nullable UUID winner) {
        this.singleWinner = winner;
    }

    private List<IRarity> getAllowedRaritiesOrLog() {
        List<IRarity> configRarities = getCompetitionFile().getAllowedRarities();
        if (configRarities.isEmpty()) {
            EvenMoreFish.getInstance().getLogger()
                    .severe("No allowed-rarities list found in " + getCompetitionFile().getFileName() + " competition config file.");
            return null;
        }
        return configRarities;
    }

    @ApiStatus.Experimental
    public void saveToFile() {
        EvenMoreFish plugin = EvenMoreFish.getInstance();
        ConfigBase base = new ConfigBase(CompetitionManager.dataFile, plugin, false);

        YamlDocument config = base.getConfig();
        config.set("comp-id", getCompetitionFile().getId());
        config.set("total-duration", maxDuration);
        config.set("time-left", timeLeft);
        if (epochStartTime != null) {
            config.set("start-time", epochStartTime);
        }
        for (CompetitionEntry entry : leaderboard.getEntries()) {
            UUID uuid = entry.getPlayer();
            config.set("leaderboard." + uuid + ".fish", entry.getFish().getRarityKey().toString());
            config.set("leaderboard." + uuid + ".value", entry.getValue());
            config.set("leaderboard." + uuid + ".time", entry.getTime());
        }
        base.save();
    }

}
