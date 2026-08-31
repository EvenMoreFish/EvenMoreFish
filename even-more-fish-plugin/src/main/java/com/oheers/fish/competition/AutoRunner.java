package com.oheers.fish.competition;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.api.EMFTimer;
import com.oheers.fish.api.Logging;
import com.oheers.fish.competition.configs.CompetitionFile;
import com.oheers.fish.utils.TimeCode;
import org.jspecify.annotations.NonNull;

import java.time.LocalTime;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class AutoRunner extends EMFTimer {

    private int lastMinute = -1;
    private final CompetitionManager manager;

    public AutoRunner(@NonNull CompetitionManager manager) {
        super(TimeUnit.SECONDS, 1);
        this.manager = manager;
    }

    /**
     * The action to be performed by this timer task.
     */
    @Override
    public void run() {
        if (hasMinuteBeenChecked()) {
            return;
        }
        TimeCode now = TimeCode.now();
        Logging.debug("AutoRunner checking TimeCode: " + now.code());

        // Beginning the competition set for schedule
        Map<TimeCode, CompetitionFile> competitions = manager.getCompetitions();
        CompetitionFile file = competitions.get(now);
        if (file == null) {
            return;
        }
        Logging.debug("AutoRunner found a competition with this TimeCode. Attempting to start.");
        if (Competition.isActive()) {
            Logging.debug("AutoRunner cannot start a competition as one is active. Attempting to hold until active is finished.");
            Competition.holdCompetition(file);
        } else {
            new Competition(file).begin();
        }
    }

    private boolean hasMinuteBeenChecked() {
        int nowMinute = LocalTime.now().getMinute();
        if (this.lastMinute != nowMinute) {
            Logging.debug("AutoRunner minute changed from " + lastMinute + " to " + nowMinute);
            this.lastMinute = nowMinute;
            return false;
        }
        return true;
    }

}
