package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.placeholders.abstracted.UserReportPlaceholder;

public class TotalCompetitionsWonPlaceholder extends UserReportPlaceholder {

    public TotalCompetitionsWonPlaceholder() {
        super(
            "total_competitions_won_",
            report -> String.valueOf(report.getCompetitionsWon()),
            "0"
        );
    }

}
