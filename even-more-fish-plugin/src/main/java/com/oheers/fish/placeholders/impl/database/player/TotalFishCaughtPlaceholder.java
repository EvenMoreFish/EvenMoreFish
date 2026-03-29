package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.placeholders.abstracted.UserReportPlaceholder;

public class TotalFishCaughtPlaceholder extends UserReportPlaceholder {

    public TotalFishCaughtPlaceholder() {
        super(
            "total_fish_caught_",
            report -> String.valueOf(report.getNumFishCaught()),
            "0"
        );
    }
}
