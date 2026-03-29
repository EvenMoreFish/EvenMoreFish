package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.placeholders.abstracted.UserReportPlaceholder;

public class TotalCompetitionsJoinedPlaceholder extends UserReportPlaceholder {

    public TotalCompetitionsJoinedPlaceholder() {
        super(
            "total_competitions_joined_",
            report -> String.valueOf(report.getCompetitionsJoined()),
            "0"
        );
    }

}
