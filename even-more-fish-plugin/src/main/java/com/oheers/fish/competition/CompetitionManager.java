package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.AbstractFileBasedManager;
import com.oheers.fish.api.Logging;
import com.oheers.fish.api.fishing.items.RarityKey;
import com.oheers.fish.competition.configs.CompetitionConversions;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.competition.types.LargestTotalCompetitionType;
import com.oheers.fish.competition.types.MostFishCompetitionType;
import com.oheers.fish.competition.types.RandomCompetitionType;
import com.oheers.fish.competition.types.ShortestFishCompetitionType;
import com.oheers.fish.competition.types.ShortestTotalCompetitionType;
import com.oheers.fish.competition.types.SpecificFishCompetitionType;
import com.oheers.fish.competition.types.SpecificRarityCompetitionType;
import com.oheers.fish.config.ConfigBase;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.MessageConfig;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.utils.TimeCode;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.logging.Level;

public class CompetitionManager extends AbstractFileBasedManager<CompetitionFile> {

    private static final CompetitionManager INSTANCE = new CompetitionManager();

    protected static final File dataFile = new File(EvenMoreFish.getInstance().getDataFolder(), "competition-data.yml.tmp");
    private static final List<CompetitionFile> held = new ArrayList<>();

    private final TreeMap<TimeCode, CompetitionFile> competitions = new TreeMap<>();
    private final AutoRunner autoRunner = new AutoRunner(this);

    protected volatile Competition activeCompetition;

    private CompetitionManager() {
        super();
    }

    public static @NonNull CompetitionManager getInstance() {
        return INSTANCE;
    }

    @Override
    protected void performPreLoadConversions() {
        new CompetitionConversions().performCheck();
    }

    @Override
    protected void loadItems() {
        loadDefaultTypes(); // Always do this first. Ensures all default types are loaded before competitions.
        loadItemsFromFiles(
                "competitions",
                CompetitionFile::new,
                CompetitionFile::getId,
                CompetitionFile::isDisabled
        );

        // Populate the competitions schedule
        competitions.clear();
        getItemMap().values().forEach(file -> {
            if (loadSpecificDayTimes(file)) {
                return;
            }
            if (loadRepeatedTiming(file)) {
                return;
            }
            EvenMoreFish.getInstance().debug(
                    Level.WARNING,
                Optional.ofNullable(file.getFile()).map(File::getName).orElse("Competition") + "'s timings are not configured properly. " +
                            "This competition will never automatically start."
            );
        });
    }

    @Override
    protected void logLoadedItems() {
        EvenMoreFish.getInstance().getLogger().info(
                "Loaded " + getItemMap().size() + " competition file(s) and " + competitions.size() + " scheduled competitions."
        );
        // Used for testing TimeCodes.
        /*
        String debug = competitions.entrySet().stream()
            .map(entry -> "Time: " + entry.getKey() + " | File: " + entry.getValue().getId())
            .collect(java.util.stream.Collectors.joining("\n"));
        System.out.println(debug);
         */
    }

    public @NonNull AutoRunner getAutoRunner() {
        return this.autoRunner;
    }

    public Map<TimeCode, CompetitionFile> getCompetitions() {
        return competitions;
    }

    private boolean loadSpecificDayTimes(@NonNull CompetitionFile file) {
        Map<DayOfWeek, List<String>> scheduledDays = file.getScheduledDays();
        if (scheduledDays.isEmpty()) {
            return false;
        }
        scheduledDays.forEach((day, times) ->
            times.forEach(time -> {
                String[] split = time.split(":");
                if (split.length != 2) {
                    Logging.warn("Invalid TimeCode in " + file.getFileName() + ": " + time);
                    return;
                }
                competitions.put(generateTimeCode(day, split[0], split[1]), file);
            })
        );
        return true;
    }

    private boolean loadRepeatedTiming(@NonNull CompetitionFile file) {
        List<String> repeatedTimes = file.getTimes();

        if (repeatedTimes.isEmpty()) {
            return false;
        }

        List<DayOfWeek> daysToUse = new ArrayList<>(Arrays.asList(DayOfWeek.values()));
        daysToUse.removeAll(file.getBlacklistedDays());

        for (String time : repeatedTimes) {
            String[] split = time.split(":");
            if (split.length != 2) {
                Logging.warn("Invalid TimeCode in " + file.getFileName() + ": " + time);
                continue;
            }
            for (DayOfWeek day : daysToUse) {
                competitions.put(generateTimeCode(day, split[0], split[1]), file);
            }
        }
        return true;
    }

    public @Nullable TimeCode generateTimeCode(@NonNull DayOfWeek day, @NonNull String hourStr, @NonNull String minuteStr) {
        Integer hour = FishUtils.getInteger(hourStr);
        Integer minute = FishUtils.getInteger(minuteStr);
        if (hour == null || minute == null) {
            return null;
        }
        return TimeCode.exact(day, hour, minute);
    }

    public int getSize() {
        return competitions.size();
    }

    public @Nullable TimeCode getNextCompetition() {
        if (competitions.isEmpty()) {
            return null;
        }
        TimeCode now = TimeCode.now();
        TimeCode next = competitions.ceilingKey(now);
        return next == null ? competitions.firstKey() : next;
    }

    public boolean hasTimings() {
        return !competitions.isEmpty();
    }

    public @Nullable CompetitionFile getFileFromId(@Nullable String id) {
        if (id == null) {
            return null;
        }
        for (CompetitionFile file : competitions.values()) {
            if (file.getId().equalsIgnoreCase(id)) {
                return file;
            }
        }
        return null;
    }

    private void loadDefaultTypes() {
        CompetitionTypeRegistry registry = CompetitionTypeRegistry.getInstance();
        registry.register(CompetitionType.DEFAULT, true); // Use the static singleton here so we only initialize LARGEST_FISH once.
        registry.register(new LargestTotalCompetitionType(), true);
        registry.register(new MostFishCompetitionType(), true);
        registry.register(new RandomCompetitionType(), true);
        registry.register(new ShortestFishCompetitionType(), true);
        registry.register(new ShortestTotalCompetitionType(), true);
        registry.register(new SpecificFishCompetitionType(), true);
        registry.register(new SpecificRarityCompetitionType(), true);
    }

    protected void setActive(@NonNull Competition competition) {
        if (activeCompetition != null) {
            activeCompetition.end(false);
        }
        this.activeCompetition = competition;
    }

    public boolean isCompetitionActive() {
        return activeCompetition != null;
    }

    public @Nullable Competition getActiveCompetition() {
        return this.activeCompetition;
    }

    public boolean isDoingFirstPlaceActionBar() {
        if (activeCompetition == null) {
            Logging.warn("Cannot check Competition#isDoingFirstPlaceActionBar: There is no active competition.");
            return false;
        }
        CompetitionType type = activeCompetition.getCompetitionType();
        if (type == null) {
            Logging.warn("Cannot check Competition#isDoingFirstPlaceActionBar: Invalid CompetitionType.");
            return false;
        }
        boolean doActionBarMessage = MessageConfig.getInstance().getConfig().getBoolean("action-bar-message");
        List<String> supportedTypes = MessageConfig.getInstance()
            .getConfig()
            .getStringList("action-bar-types");
        boolean isSupportedActionBarType = activeCompetition != null && supportedTypes.contains(type.getKey());
        return doActionBarMessage && isSupportedActionBarType;
    }

    public @NonNull EMFMessage getNextCompetitionMessage() {
        if (activeCompetition != null) {
            return EMFSingleMessage.empty();
        }

        long remainingTime = getRemainingTime();
        if (remainingTime == -1) {
            return ConfigMessage.PLACEHOLDER_NO_COMPETITIONS_SCHEDULED.getMessage();
        }

        EMFMessage message = ConfigMessage.PLACEHOLDER_TIME_REMAINING_INACTIVE.getMessage();
        message.setDays(Long.toString(remainingTime / 1440));
        message.setHours(Long.toString((remainingTime % 1440) / 60));
        message.setMinutes(Long.toString((((remainingTime % 1440) % 60) % 60)));

        return message;
    }

    private long getRemainingTime() {
        TimeCode next = getNextCompetition();
        if (next == null) {
            return -1L;
        }
        long startTime = next.toMillis();
        long currentTime = System.currentTimeMillis();
        return Duration.ofMillis(startTime - currentTime).toMinutes();
    }

    public void resumeFromFile() {
        EvenMoreFish plugin = EvenMoreFish.getInstance();
        if (!dataFile.exists()) {
            return;
        }
        ConfigBase base = new ConfigBase(dataFile, plugin, false);

        YamlDocument config = base.getConfig();
        CompetitionResumeSnapshot snapshot = CompetitionResumeSnapshot.read(config);
        if (snapshot == null) {
            archiveUnrestorableBackup("Cannot safely resume the competition backup: its snapshot is legacy or incomplete. " +
                "Legacy start-time values were stored in epoch seconds.");
            return;
        }
        if (!snapshot.resumeSupported()) {
            archiveUnrestorableBackup("Cannot safely resume the competition backup: its competition type has no restore contract.");
            return;
        }
        String id = config.getString("comp-id");
        long totalDuration = config.getLong("total-duration");
        long timeLeft = config.getLong("time-left");
        if (totalDuration <= 0 || timeLeft <= 0 || timeLeft > totalDuration) {
            archiveUnrestorableBackup("Cannot resume competition backup with invalid time values.");
            return;
        }

        CompetitionFile file = CompetitionManager.getInstance().getFileFromId(id);
        if (file == null) {
            archiveUnrestorableBackup("Cannot resume competition backup: competition " + id + " is not configured.");
            return;
        }

        CompetitionType configuredType = file.getType();
        CompetitionType resolvedType = CompetitionTypeRegistry.getInstance().get(snapshot.resolvedType());
        if (configuredType == null || !configuredType.getKey().equalsIgnoreCase(snapshot.configuredType())
            || !configuredType.supportsResume() || resolvedType == null || resolvedType instanceof CompetitionType.Random
            || !resolvedType.supportsResume()) {
            archiveUnrestorableBackup("Cannot resume competition backup: its saved type is missing or no longer matches the config.");
            return;
        }
        if (!(configuredType instanceof CompetitionType.Random)
            && !configuredType.getKey().equalsIgnoreCase(resolvedType.getKey())) {
            archiveUnrestorableBackup("Cannot resume competition backup: its resolved type differs from the configured type.");
            return;
        }

        Competition competition = new Competition(file, false);
        competition.timeLeft = timeLeft;
        competition.maxDuration = totalDuration;
        competition.adminStarted = true;
        competition.setNumberNeeded(snapshot.numberNeeded());
        competition.setStartTime(Instant.ofEpochMilli(snapshot.startTimeMillis()));
        CompetitionType restoredType = resolvedType.freshInstance();
        if (restoredType.isSingleReward() && snapshot.numberNeeded() <= 0) {
            archiveUnrestorableBackup("Cannot resume competition backup: its winning threshold is invalid.");
            return;
        }
        if (!restoredType.restoreResumeState(snapshot.typeState(), competition)) {
            archiveUnrestorableBackup("Cannot resume competition backup: its saved target is missing or invalid.");
            return;
        }
        competition.setCompetitionType(configuredType instanceof CompetitionType.Random random
            ? new CompetitionType.Forwarding(random, restoredType) : restoredType);

        competition.leaderboard = new Leaderboard(competition.getCompetitionType());
        Section leaderboardSection = config.getSection("leaderboard");
        if (leaderboardSection != null) {
            for (String key : leaderboardSection.getRoutesAsStrings(false)) {
                Section entrySection = leaderboardSection.getSection(key);
                if (entrySection == null) {
                    archiveUnrestorableBackup("Cannot resume competition backup: a leaderboard entry is incomplete.");
                    return;
                }
                UUID player;
                try {
                    player = UUID.fromString(key);
                } catch (IllegalArgumentException exception) {
                    archiveUnrestorableBackup("Cannot resume competition backup: invalid player UUID " + key + ".");
                    return;
                }
                String fishStr = entrySection.getString("fish");
                RarityKey rarityKey = fishStr == null ? null : RarityKey.of(fishStr);
                if (rarityKey == null) {
                    archiveUnrestorableBackup("Cannot resume competition backup: fish " + fishStr + " is missing.");
                    return;
                }
                CompetitionEntry entry = new CompetitionEntry(player, rarityKey.getFish(), competition.getCompetitionType());
                entry.value = entrySection.getFloat("value", Float.NaN);
                entry.time = entrySection.getLong("time", -1L);
                if (!Float.isFinite(entry.value) || entry.time <= 0 || !competition.leaderboard.restoreEntry(entry)) {
                    archiveUnrestorableBackup("Cannot resume competition backup: a leaderboard score is invalid or collides with another entry.");
                    return;
                }
            }
        }
        if (!competition.begin()) {
            archiveUnrestorableBackup("Cannot resume competition backup: the restored competition could not start.");
            return;
        }
        dataFile.delete();
    }

    private void archiveUnrestorableBackup(String reason) {
        File archive = dataFile.toPath().resolveSibling(dataFile.getName() + ".unrestorable-" + UUID.randomUUID() + ".yml").toFile();
        try {
            Files.move(dataFile.toPath(), archive.toPath());
            Logging.warn(reason + " Backup preserved at " + archive.getAbsolutePath());
        } catch (IOException exception) {
            Logging.warn(reason + " Could not archive the backup; it remains at " + dataFile.getAbsolutePath());
            EvenMoreFish.getInstance().getLogger().log(Level.SEVERE, "Could not archive competition backup", exception);
        }
    }

    public void holdCompetition(@NonNull CompetitionFile file) {
        if (!MainConfig.getInstance().shouldCompetitionHold()) {
            Logging.debug("Could not hold a competition as the feature is disabled.");
            return;
        }
        held.add(file);
    }

    protected void checkHeldCompetition() {
        if (held.isEmpty()) {
            Logging.debug("No competitions have been held back.");
            return;
        }
        CompetitionFile file = held.removeFirst();
        if (file != null) {
            Logging.info("A competition was held back during this one. It will now be started.");
            new Competition(file).begin();
        }
    }

}

