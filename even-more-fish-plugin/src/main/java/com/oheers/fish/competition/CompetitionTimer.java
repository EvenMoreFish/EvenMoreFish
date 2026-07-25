package com.oheers.fish.competition;

import com.oheers.fish.api.EMFTimer;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.TimeUnit;

public class CompetitionTimer extends EMFTimer {

    private final Competition competition;

    public CompetitionTimer(@NonNull Competition competition) {
        super(TimeUnit.SECONDS, 1);
        this.competition = competition;
    }

    @Override
    public void run() {
        competition.getStatusBar().timerUpdate(competition.getTimeLeft(), competition.getMaxDuration());
        if (competition.decreaseTime()) {
            stop();
        }
    }

}
