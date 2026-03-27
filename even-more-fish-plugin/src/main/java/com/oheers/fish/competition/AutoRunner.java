package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.EMFTimer;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.utils.TimeCode;

import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AutoRunner extends EMFTimer {

    private int lastMinute = -1;

    public AutoRunner() {
        super(TimeUnit.SECONDS, 1);
    }

    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {
        EvenMoreFish.getScheduler().runTask(
            () -> {
                if (hasMinuteBeenChecked()) {
                    return;
                }
                TimeCode now = TimeCode.now();

                // Beginning the competition set for schedule
                Map<TimeCode, CompetitionFile> competitions = EvenMoreFish.getInstance().getCompetitionQueue().getCompetitions();
                CompetitionFile file = competitions.get(now);
                if (file != null && !Competition.isActive()) {
                    new Competition(file).begin();
                }
            }
        );
    }

    private boolean hasMinuteBeenChecked() {
        int nowMinute = LocalTime.now().getMinute();
        if (this.lastMinute != nowMinute) {
            this.lastMinute = nowMinute;
            return false;
        }
        return true;
    }

}
