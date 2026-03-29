package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.placeholders.abstracted.UserReportPlaceholder;

public class TotalFishSoldPlaceholder extends UserReportPlaceholder {

    public TotalFishSoldPlaceholder() {
        super(
            "total_fish_sold_",
            report -> String.valueOf(report.getFishSold()),
            "0"
        );
    }

}
