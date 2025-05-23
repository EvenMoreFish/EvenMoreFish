package com.oheers.fish.competition;

import com.github.Anon8281.universalScheduler.UniversalRunnable;
import com.github.Anon8281.universalScheduler.scheduling.tasks.MyScheduledTask;
import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.EMFCompetitionEndEvent;
import com.oheers.fish.api.EMFCompetitionStartEvent;
import com.oheers.fish.api.reward.Reward;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.MessageConfig;
import com.oheers.fish.database.model.CompetitionReport;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.fishing.items.FishManager;
import com.oheers.fish.fishing.items.Rarity;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFListMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.Sound;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
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
    private MyScheduledTask timingSystem;
    private CompetitionFile competitionFile;
    private int numberNeeded = 0;
    private Player singleWinner = null;

    public Competition(final @NotNull CompetitionFile competitionFile) {
        this.competitionFile = competitionFile;
        this.competitionName = competitionFile.getId();
        this.playersNeeded = competitionFile.getPlayersNeeded();
        this.startSound = competitionFile.getStartSound();
        this.maxDuration = competitionFile.getDuration() * 60L;
        this.alertTimes = competitionFile.getAlertTimes();
        this.rewards = competitionFile.getRewards();
        this.competitionType = competitionFile.getType();
        this.statusBar = competitionFile.createBossbar();
        this.numberNeeded = competitionFile.getNumberNeeded();
    }

    public Competition(final Integer duration, final CompetitionType type) {
        this.maxDuration = duration;
        this.alertTimes = new ArrayList<>();
        this.rewards = new HashMap<>();
        this.competitionType = type;
    }

    /**
     * Sets the maximum duration of the competition in seconds.
     * @param duration The maximum duration of the competition in seconds.
     */
    public void setMaxDuration(int duration) {
        this.maxDuration = duration;
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

    public void begin() {
        try {
            if (!isAdminStarted() && EvenMoreFish.getInstance().getVisibleOnlinePlayers().size() < playersNeeded) {
                ConfigMessage.NOT_ENOUGH_PLAYERS.getMessage().broadcast();
                return;
            }

            // Make sure the active competition has ended.
            if (active != null) {
                active.end(false);
            }
            active = this;

            CompetitionStrategy strategy = competitionType.getStrategy();
            if (!strategy.begin(this)) {
                active = null;
                return;
            }

            this.timeLeft = this.maxDuration;

            this.leaderboard = new Leaderboard(competitionType);

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
        } catch (Exception ex) {
            end(true);
        }
    }

    public void end(boolean startFail) {
        // print leaderboard
        if (this.timingSystem != null) {
            this.timingSystem.cancel();
        }
        if (statusBar != null) {
            statusBar.hide();
        }
        try {
            if (!startFail) {
                EMFCompetitionEndEvent endEvent = new EMFCompetitionEndEvent(this);
                endEvent.callEvent();
                for (Player player : Bukkit.getOnlinePlayers()) {
                    ConfigMessage.COMPETITION_END.getMessage().send(player);
                    sendLeaderboard(player);
                }
                if (competitionType.getStrategy().isSingleReward() && singleWinner != null) {
                    singleReward(singleWinner);
                } else {
                    handleRewards();
                }
                if (originallyRandom) {
                    competitionType = CompetitionType.RANDOM;
                }

                if (MainConfig.getInstance().isDatabaseOnline()) {
                    Competition competitionRef = this;
                    EvenMoreFish.getInstance().getCompetitionDataManager().update(competitionRef.competitionName, new CompetitionReport(competitionRef, competitionRef.startTime, LocalDateTime.now()));
                }

                leaderboard.clear();
            }
        } catch (Exception exception) {
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "An exception was thrown while the competition was being ended!", exception);
        } finally {
            active = null;
        }
    }

    // Starts a runnable to decrease the time left by 1s each second
    private void initTimer() {
        this.timingSystem = new UniversalRunnable() {
            @Override
            public void run() {
                statusBar.timerUpdate(timeLeft, maxDuration);
                if (decreaseTime()) {
                    cancel();
                }
            }
        }.runTaskTimer(EvenMoreFish.getInstance(), 0, 20);
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

    /**
     * On servers with low tps, 20 ticks != 1 second, so competitions can last for considerably longer, this re-calibrates
     * the time left with an epoch version of the time left. It runs through each second skipped to make sure all necessary
     * processes take place i.e. alerts.
     */
    private boolean decreaseTime() {
        long lagDif;
        long current = Instant.now().getEpochSecond();

        timeLeft = maxDuration - (current - epochStartTime);
        // +1 to counteract the seconds starting on 0 (or something like that)
        if ((lagDif = (current - epochStartTime) + 1) != maxDuration - timeLeft) {
            for (long i = maxDuration - timeLeft; i < lagDif; i++) {
                if (processCompetitionSecond(timeLeft)) {
                    return true;
                }
                timeLeft--;
            }
        }
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

    private @NotNull EMFMessage buildLeaderboardMessage(List<CompetitionEntry> entries, List<String> competitionColours, boolean isConsole) {
        if (entries == null) {
            entries = List.of();
        }

        int maxCount = MessageConfig.getInstance().getLeaderboardCount();

        EMFListMessage builder = EMFListMessage.empty();
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
            message.setPositionColour(competitionColours.get(pos - 1));
            message.setRarity(entry.getFish().getRarity().getDisplayName());
            message.setFishCaught(entry.getFish().getDisplayName());

            builder.appendMessage(message);
        }

        return builder;
    }

    private void handleDatabaseUpdates(CompetitionEntry entry, boolean isTopEntry) {
        if (!MainConfig.getInstance().isDatabaseOnline()) {
            return;
        }

        UserReport userReport = EvenMoreFish.getInstance().getUserReportDataManager().get(String.valueOf(entry.getPlayer()));
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

        if (MainConfig.getInstance().isDatabaseOnline() && !entries.isEmpty()) {
            handleDatabaseUpdates(leaderboard.getTopEntry(), true); // Top entry
        }

        for (CompetitionEntry entry : entries) {
            Player player = Bukkit.getPlayer(entry.getPlayer());

            // If the player is null, increment the place and continue
            if (player == null) {
                rewardPlace++;
                continue;
            }

            // Does the player's place have rewards?
            if (rewards.containsKey(rewardPlace)) {
                rewards.get(rewardPlace).forEach(reward -> reward.rewardPlayer(player, null));
            } else {
                // Default to participation rewards if not.
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
            return ConfigMessage.PLACEHOLDER_TIME_REMAINING_DURING_COMP.getMessage();
        }

        int remainingTime = getRemainingTime();

        EMFMessage message = ConfigMessage.PLACEHOLDER_TIME_REMAINING.getMessage();
        message.setDays(Integer.toString(remainingTime / 1440));
        message.setHours(Integer.toString((remainingTime % 1440) / 60));
        message.setMinutes(Integer.toString((((remainingTime % 1440) % 60) % 60)));

        return message;
    }

    private static int getRemainingTime() {
        int competitionStartTime = EvenMoreFish.getInstance().getCompetitionQueue().getNextCompetition();
        int currentTime = AutoRunner.getCurrentTimeCode();
        if (competitionStartTime > currentTime) {
            return competitionStartTime - currentTime;
        }

        return getRemainingTimeOverWeek(competitionStartTime, currentTime);
    }

    // time left of the current week + the time next week until next competition
    private static int getRemainingTimeOverWeek(int competitionStartTime, int currentTime) {
        return (10080 - currentTime) + competitionStartTime;
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

    public boolean chooseFish() {
        List<Rarity> configRarities = getCompetitionFile().getAllowedRarities();
        final Logger logger = EvenMoreFish.getInstance().getLogger();
        if (configRarities.isEmpty()) {
            logger.severe(() -> "No allowed-rarities list found in the " + getCompetitionFile().getFileName() + " competition config file.");
            return false;
        }

        List<Fish> fish = new ArrayList<>();
        List<Rarity> allowedRarities = new ArrayList<>();
        double totalWeight = 0;

        for (Rarity rarity : configRarities) {
            fish.addAll(rarity.getOriginalFishList());
            allowedRarities.add(rarity);
            totalWeight += rarity.getWeight();
        }

        int idx = 0;
        for (double r = Math.random() * totalWeight; idx < allowedRarities.size() - 1; ++idx) {
            r -= allowedRarities.get(idx).getWeight();
            if (r <= 0.0) {
                break;
            }
        }

        try {
            Fish selectedFish = FishManager.getInstance().getFish(allowedRarities.get(idx), null, null, 1.0d, null, false, null);
            if (selectedFish == null) {
                // For the catch block to catch.
                throw new IllegalArgumentException();
            }
            this.selectedFish = selectedFish;
            return true;
        } catch (IllegalArgumentException | IndexOutOfBoundsException exception) {
            logger.severe(() -> "Could not load: %s because a random fish could not be chosen. %nIf you need support, please provide the following information:".formatted(getCompetitionName()));
            logger.severe(() -> "fish.size(): %s".formatted(fish.size()));
            logger.severe(() -> "allowedRarities.size(): %s".formatted(allowedRarities.size()));
            // Also log the exception
            logger.log(Level.SEVERE, exception.getMessage(), exception);
            return false;
        }
    }

    public boolean chooseRarity() {
        List<Rarity> configRarities = getCompetitionFile().getAllowedRarities();

        if (configRarities.isEmpty()) {
            EvenMoreFish.getInstance().getLogger().severe("No allowed-rarities list found in the " + getCompetitionFile().getFileName() + " competition config file.");
            return false;
        }

        try {
            Rarity rarity = configRarities.get(EvenMoreFish.getInstance().getRandom().nextInt(configRarities.size()));
            if (rarity != null) {
                this.selectedRarity = rarity;
                return true;
            }
            this.selectedRarity = FishManager.getInstance().getRandomWeightedRarity(null, 0, null, Set.copyOf(FishManager.getInstance().getRarityMap().values()));
            return true;
        } catch (IllegalArgumentException exception) {
            EvenMoreFish.getInstance()
                .getLogger()
                .severe("Could not load: " + getCompetitionName() + " because a random rarity could not be chosen. \nIf you need support, please provide the following information:");
            EvenMoreFish.getInstance().getLogger().severe("rarities.size(): " + FishManager.getInstance().getRarityMap().size());
            EvenMoreFish.getInstance().getLogger().severe("configRarities.size(): " + configRarities.size());
            // Also log the exception
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, exception.getMessage(), exception);
            return false;
        }
    }

    public void setSingleWinner(@Nullable Player player) {
        this.singleWinner = player;
    }

}
