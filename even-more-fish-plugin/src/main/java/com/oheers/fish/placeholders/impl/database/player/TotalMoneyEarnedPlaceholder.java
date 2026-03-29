package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.placeholders.abstracted.UserReportPlaceholder;

public class TotalMoneyEarnedPlaceholder extends UserReportPlaceholder {


    public TotalMoneyEarnedPlaceholder() {
        super(
            "total_money_earned_",
            report -> format(report.getMoneyEarned()),
            format(0D)
        );
    }

    private static String format(double value) {
        return String.format("%.2f", value);
    }

}
