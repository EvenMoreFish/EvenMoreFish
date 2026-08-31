package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.AbstractFileBasedManager;
import com.oheers.fish.api.Logging;
import com.oheers.fish.competition.configs.CompetitionConversions;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.competition.types.LargestFishCompetitionType;
import com.oheers.fish.competition.types.LargestTotalCompetitionType;
import com.oheers.fish.competition.types.MostFishCompetitionType;
import com.oheers.fish.competition.types.RandomCompetitionType;
import com.oheers.fish.competition.types.ShortestFishCompetitionType;
import com.oheers.fish.competition.types.ShortestTotalCompetitionType;
import com.oheers.fish.competition.types.SpecificFishCompetitionType;
import com.oheers.fish.competition.types.SpecificRarityCompetitionType;
import com.oheers.fish.fishing.rods.RodManager;
import com.oheers.fish.utils.TimeCode;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.io.File;
import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.TreeMap;
import java.util.logging.Level;

public class CompetitionManager extends AbstractFileBasedManager<CompetitionFile> {

    private static final CompetitionManager INSTANCE = new CompetitionManager();

    private final TreeMap<TimeCode, CompetitionFile> competitions = new TreeMap<>(TimeCode.getComparator());
    private final AutoRunner autoRunner = new AutoRunner(this);

    private CompetitionManager() {
        super(RodManager.getInstance());
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

}

