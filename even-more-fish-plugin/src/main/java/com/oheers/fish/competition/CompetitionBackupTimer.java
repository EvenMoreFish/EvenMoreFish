package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.EMFTimer;
import com.oheers.fish.config.MainConfig;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

/**
 * Saves the competition to file on a configurable interval.
 * Used for restoring after server crashes.
 */
public class CompetitionBackupTimer extends EMFTimer {

    private final Competition competition;

    public CompetitionBackupTimer(@NotNull Competition competition) {
        super(TimeUnit.SECONDS, MainConfig.getInstance().getCompetitionBackupInterval());
        this.competition = competition;
    }

    @Override
    public void run() {
        EvenMoreFish.getScheduler().runTask(competition::saveToFile);
    }

}
