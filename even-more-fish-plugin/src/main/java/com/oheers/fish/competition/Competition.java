package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.EMFCompetitionEndEvent;
import com.oheers.fish.api.EMFCompetitionStartEvent;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.config.MessageConfig;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.database.model.CompetitionReport;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Competition {

    private static Competition active;
    private boolean originallyRandom;
    private Leaderboard leaderboard;
    private CompetitionType competitionType;
    private Fish selectedFish;
    private Rarity selectedRarity;
    private String competitionName;
    private boolean adminStarted = false;
    private EMFMessage startMessage;
    private long maxDuration;
    private long timeLeft;
    private Bar statusBar;
    private long epochStartTime;
    private LocalDateTime startTime;
    private final List<Long> alertTimes;
    private final Map<Integer, List<Reward>> rewards;
    private int playersNeeded;
    private Sound startSound;
    private CompetitionTimer timingSystem;
    private CompetitionFile competitionFile;
    private int numberNeeded = 0;
    private Player singleWinner = null;

    public Competition(final @NotNull CompetitionFile competitionFile) {
        this.competitionFile = competitionFile;
        this.competitionName = competitionFile.getId();
        this.playersNeeded = competitionFile.getPlayersNeeded();
        this.startSound = competitionFile.getStartSound();
        this.maxDuration = competitionFile.getDuration() * 60L;
        this.timeLeft = this.maxDuration;
        this.alertTimes = competitionFile.getAlertTimes();
        this.rewards = competitionFile.getRewards();
        this.competitionType = competitionFile.getType();
        this.numberNeeded = competitionFile.getNumberNeeded();
    }

    /**
     * @return A valid bossbar for this competition. Null if it should not be shown.
     */
    private @NotNull Bar createBossbar() {
        Bar bar = new Bar();
        bar.setShouldShow(competitionFile.shouldShowBossbar());
        bar.setColour(competitionFile.getBossbarColour());

        EMFSingleMessage prefix = competitionFile.getBossbarPrefix();
        if (selectedRarity != null) {
            prefix.setRarity(selectedRarity.getDisplayName());
        } else if (selectedFish != null) {
            prefix.setRarity(selectedFish.getRarity().getDisplayName());
            prefix.setVariable("{fish}", selectedFish.getDisplayName());
        }
        bar.setPrefix(prefix, competitionType);
        return bar;
    }

    public Competition(final long duration, final CompetitionType type) {
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

    public static boolean isActive() {
        return getCurrentlyActive() != null;
    }

    public void setOriginallyRandom(boolean originallyRandom) {
        this.originallyRandom = originallyRandom;
    }

    public static @Nullable Competition getCurrentlyActive() {
        return active;
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
        try {
            if (!isAdminStarted() && !isPlayerRequirementMet()) {
                ConfigMessage.NOT_ENOUGH_PLAYERS.getMessage().broadcast();
                return false;
            }

            // Make sure the active competition has ended.
            if (!ended()) {
                active.end(false);
            }

            active = this;

            CompetitionStrategy strategy = competitionType.getStrategy();
            if (!strategy.begin(this)) {
                active = null;
                return false;
            }

            this.leaderboard = new Leaderboard(competitionType);

            if (this.statusBar == null) {
                this.statusBar = createBossbar();
            }
            statusBar.show();

            initTimer();
            announceBegin();
            EMFCompetitionStartEvent startEvent = new EMFCompetitionStartEvent(this);
            Bukkit.getServer().getPluginManager().callEvent(startEvent);

            final Instant now = Instant.now();
            this.epochStartTime = now.getEpochSecond();
            this.startTime = LocalDateTime.ofInstant(now, ZoneId.systemDefault());

            // Execute start commands
            getCompetitionFile().getStartCommands().forEach(command -> Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command));

            EvenMoreFish.getInstance().getDecidedRarities().clear();
            return true;
        } catch (Exception ex) {
            Logging.error("An exception was thrown while starting the competition.", ex);
            end(true);
            return false;
        }
    }

    public void end(boolean startFail) {
        if (ended()) {
            return;
        }
        // Print leaderboard
        if (timingSystem != null) {
            timingSystem.stop();
        }
        if (statusBar != null) {
            statusBar.hide();
        }

        if (startFail) {
            active = null;
            return;
        }

        try {
            fireEndEvent();
            notifyPlayers();
            processRewards();
            resetCompetitionTypeIfRandom();
            updateDatabase();
            leaderboard.clear();
        } catch (Exception exception) {
            EvenMoreFish.getInstance().getLogger().log(
                    Level.SEVERE,
                    "An exception was thrown while the competition was being ended!",
                    exception
            );
        } finally {
            active = null;
        }
    }

    public boolean ended() {
        return active == null;
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
        if (competitionType.getStrategy().isSingleReward() && singleWinner != null) {
            singleReward(singleWinner);
        } else {
            handleRewards();
        }
    }

    private void resetCompetitionTypeIfRandom() {
        if (originallyRandom) {
            competitionType = CompetitionType.RANDOM;
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
            EMFMessage message = getTypeFormat(ConfigMessage.TIME_ALERT);
            message.broadcast();
        } else if (timeLeft <= 0) {
            end(false);
            return true;
        }
        return false;
    }

    /**
     * This creates a message object and applies all the settings to it to make it able to use the {type} variable. It
     * takes into consideration whether it's a specific fish/rarity competition.
     *
     * @param configMessage The configmessage to use. Must have the {type} variable in it.
     * @return A message object that's pre-set to be compatible for the time remaining.
     */
    private @NotNull EMFMessage getTypeFormat(ConfigMessage configMessage) {
        return competitionType.getStrategy().getTypeFormat(this, configMessage);
    }

    protected boolean decreaseTime() {
        if (processCompetitionSecond(timeLeft)) {
            return true;
        }
        timeLeft--;
        return false;
    }

    /**
     * Calculates whether to send the "new first place" notification as an actionbar message or directly into chat.
     *
     * @return A boolean, true = do it in actionbar.
     */
    public static boolean isDoingFirstPlaceActionBar() {
        boolean doActionBarMessage = MessageConfig.getInstance().getConfig().getBoolean("action-bar-message");
        List<String> supportedTypes = MessageConfig.getInstance()
                .getConfig()
                .getStringList("action-bar-types");
        boolean isSupportedActionBarType = active != null && supportedTypes.contains(active.competitionType.toString());
        return doActionBarMessage && isSupportedActionBarType;
    }

    public void applyToLeaderboard(Fish fish, Player fisher) {
        competitionType.getStrategy().applyToLeaderboard(fish, fisher, leaderboard, this);
    }

    public void announceBegin() {
        startMessage = competitionType.getStrategy().getBeginMessage(this, competitionType);
        startMessage.broadcast();

        if (startSound != null) {
            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), startSound, 10f, 1f));
        }
    }

    public void sendLeaderboard(@NotNull CommandSender sender) {
        if (!isActive()) {
            ConfigMessage.NO_COMPETITION_RUNNING.getMessage().send(sender);
            return;
        }
        if (leaderboard.getSize() == 0) {
            ConfigMessage.NO_FISH_CAUGHT.getMessage().send(sender);
            return;
        }

        List<String> competitionColours = competitionFile.getLeaderboardColours();
        List<CompetitionEntry> entries = leaderboard.getEntries();

        boolean isConsole = !(sender instanceof Player);
        EMFMessage leaderboardMessage = buildLeaderboardMessage(entries, competitionColours, isConsole);
        leaderboardMessage.send(sender);

        EMFMessage message = ConfigMessage.LEADERBOARD_TOTAL_PLAYERS.getMessage();
        message.setAmount(Integer.toString(leaderboard.getSize()));
        message.send(sender);
    }

    private @NotNull EMFListMessage buildLeaderboardMessage(List<CompetitionEntry> entries, List<String> competitionColours, boolean isConsole) {
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
            EMFMessage message;
            if (isConsole) {
                message = competitionType.getStrategy().getSingleConsoleLeaderboardMessage(entry);
            } else {
                message = competitionType.getStrategy().getSinglePlayerLeaderboard(entry);
            }

            // Format remaining variables
            OfflinePlayer player = Bukkit.getOfflinePlayer(entry.getPlayer());

            String name = player.getName() == null ? "Unknown" : player.getName();
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
            if (!((competitionType == CompetitionType.SPECIFIC_FISH || competitionType == CompetitionType.SPECIFIC_RARITY) && numberNeeded == 1)) {
                ConfigMessage.NO_WINNERS.getMessage().broadcast();
            }
            return;
        }

        int rewardPlace = 1;

        List<CompetitionEntry> entries = leaderboard.getEntries();

        if (DatabaseUtil.isDatabaseOnline() && !entries.isEmpty()) {
            handleDatabaseUpdates(leaderboard.getTopEntry(), true); // Top entry
        }

        for (CompetitionEntry entry : entries) {
            Player player = Bukkit.getPlayer(entry.getPlayer());

            // If the player is null, increment the place and continue
            if (player == null) {
                rewardPlace++;
                continue;
            }

            // Does the player's place have reward?
            if (rewards.containsKey(rewardPlace)) {
                rewards.get(rewardPlace).forEach(reward -> reward.rewardPlayer(player, null));
            } else {
                // Default to participation reward if not.
                List<Reward> participation = rewards.get(-1);
                if (participation != null) {
                    participation.forEach(reward -> reward.rewardPlayer(player, null));
                }
            }

            handleDatabaseUpdates(entry, false);

            // Increment the place
            rewardPlace++;
        }
    }

    private void singleReward(Player player) {
        EMFMessage message = getTypeFormat(ConfigMessage.COMPETITION_SINGLE_WINNER);
        message.setPlayer(player);
        message.setCompetitionType(competitionType.getTypeVariable().getMessage());

        message.broadcast();

        if (!rewards.isEmpty()) {
            for (Reward reward : rewards.get(1)) {
                reward.rewardPlayer(player, null);
            }
        }
    }

    public @NotNull Bar getStatusBar() {
        return this.statusBar;
    }

    public @NotNull CompetitionType getCompetitionType() {
        return competitionType;
    }

    public void setNumberNeeded(int numberNeeded) {
        this.numberNeeded = numberNeeded;
    }

    public int getLeaderboardSize() {
        return leaderboard.getSize();
    }

    public @NotNull Leaderboard getLeaderboard() {
        return leaderboard;
    }

    public @Nullable EMFMessage getStartMessage() {
        return startMessage;
    }

    public @NotNull String getCompetitionName() {
        return competitionName;
    }

    public @NotNull CompetitionFile getCompetitionFile() {
        return this.competitionFile;
    }

    public static @NotNull EMFMessage getNextCompetitionMessage() {
        if (Competition.isActive()) {
            return EMFSingleMessage.empty();
        }

        long remainingTime = getRemainingTime();

        EMFMessage message = ConfigMessage.PLACEHOLDER_TIME_REMAINING_INACTIVE.getMessage();
        message.setDays(Long.toString(remainingTime / 1440));
        message.setHours(Long.toString((remainingTime % 1440) / 60));
        message.setMinutes(Long.toString((((remainingTime % 1440) % 60) % 60)));

        return message;
    }

    private static long getRemainingTime() {
        long startTime = EvenMoreFish.getInstance().getCompetitionQueue().getNextCompetition().toMillis();
        long currentTime = System.currentTimeMillis();
        return Duration.ofMillis(startTime - currentTime).toMinutes();
    }

    public void setCompetitionType(CompetitionType competitionType) {
        this.competitionType = competitionType;
    }

    public @Nullable Fish getSelectedFish() {
        return selectedFish;
    }

    public @Nullable Rarity getSelectedRarity() {
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
        List<Rarity> configRarities = getAllowedRaritiesOrLog();
        if (configRarities == null) return false;

        final Logger logger = EvenMoreFish.getInstance().getLogger();

        List<Fish> fishPool = new ArrayList<>();
        for (Rarity rarity : configRarities) {
            fishPool.addAll(rarity.getOriginalFishList());
        }

        if (fishPool.isEmpty()) {
            logger.severe("No fish available in allowed rarities for " + getCompetitionName());
            return false;
        }

        try {
            Fish selectedFish = FishManager.getInstance().getRandomWeightedFish(fishPool, 1.0d, null);
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
        List<Rarity> configRarities = getAllowedRaritiesOrLog();
        if (configRarities == null) return false;

        final Logger logger = EvenMoreFish.getInstance().getLogger();

        try {
            Rarity rarity = configRarities.get(EvenMoreFish.getInstance().getRandom().nextInt(configRarities.size()));

            if (rarity == null) {
                rarity = FishManager.getInstance().getRandomWeightedRarity(
                        null, 0, Collections.emptySet(),
                        Set.copyOf(FishManager.getInstance().getRarityMap().values()), null
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

    public void setSingleWinner(@Nullable Player player) {
        this.singleWinner = player;
    }

    private List<Rarity> getAllowedRaritiesOrLog() {
        List<Rarity> configRarities = getCompetitionFile().getAllowedRarities();
        if (configRarities.isEmpty()) {
            EvenMoreFish.getInstance().getLogger()
                    .severe("No allowed-rarities list found in " + getCompetitionFile().getFileName() + " competition config file.");
            return null;
        }
        return configRarities;
    }

}
