package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.AbstractFileBasedManager;
import com.oheers.fish.competition.configs.CompetitionConversions;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.fishing.rods.RodManager;
import com.oheers.fish.utils.TimeCode;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.logging.Level;

public class CompetitionQueue extends AbstractFileBasedManager<CompetitionFile> {

    private final TreeMap<TimeCode, CompetitionFile> competitions = new TreeMap<>(TimeCode.getComparator());

    public CompetitionQueue() {
        super(RodManager.getInstance());
    }

    @Override
    protected void performPreLoadConversions() {
        new CompetitionConversions().performCheck();
    }

    @Override
    protected void loadItems() {
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
                    file.getFile().getName() + "'s timings are not configured properly. " +
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

    public Map<TimeCode, CompetitionFile> getCompetitions() {
        return competitions;
    }

    private boolean loadSpecificDayTimes(@NotNull CompetitionFile file) {
        Map<DayOfWeek, List<String>> scheduledDays = file.getScheduledDays();
        if (scheduledDays.isEmpty()) {
            return false;
        }
        scheduledDays.forEach((day, times) ->
                times.forEach(time ->
                        competitions.put(generateTimeCode(day, time), file)
                )
        );
        return true;
    }

    private boolean loadRepeatedTiming(@NotNull CompetitionFile file) {
        List<String> repeatedTimes = file.getTimes();

        if (repeatedTimes.isEmpty()) {
            return false;
        }

        List<DayOfWeek> daysToUse = new ArrayList<>(Arrays.asList(DayOfWeek.values()));
        daysToUse.removeAll(file.getBlacklistedDays());

        for (String time : repeatedTimes) {
            for (DayOfWeek day : daysToUse) {
                competitions.put(generateTimeCode(day, time), file);
            }
        }
        return true;
    }

    public @Nullable TimeCode generateTimeCode(@NotNull DayOfWeek day, @NotNull String hourMinute) {
        String[] time = hourMinute.split(":");
        if (time.length != 2) {
            return null;
        }
        Integer hour = FishUtils.getInteger(time[0]);
        Integer minute = FishUtils.getInteger(time[1]);
        if (hour == null || minute == null) {
            return null;
        }
        return TimeCode.exact(day, hour, minute);
    }

    public int getSize() {
        return competitions.size();
    }

    public TimeCode getNextCompetition() {
        TimeCode now = TimeCode.now();
        if (competitions.containsKey(now)) {
            return now;
        }

        competitions.put(now, null);
        TimeCode nextCode = findNextCode(now);
        competitions.remove(now);

        return nextCode;
    }

    private TimeCode findNextCode(@NotNull TimeCode now) {
        List<TimeCode> timeCodes = new ArrayList<>(competitions.keySet());
        int position = timeCodes.indexOf(now);

        if (position == competitions.size() - 1) {
            return timeCodes.get(0);
        }

        return timeCodes.get(position + 1);
    }

}

