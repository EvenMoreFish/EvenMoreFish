package com.oheers.fish.placeholders.impl.database.player;

import com.oheers.fish.api.Logging;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.placeholders.abstracted.EMFPlaceholder;
import com.oheers.fish.placeholders.abstracted.UserReportPlaceholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Function;

public class TotalFishSoldPlaceholder extends UserReportPlaceholder {

    public TotalFishSoldPlaceholder() {
        super(
            "total_fish_sold_",
            report -> String.valueOf(report.getFishSold()),
            "0"
        );
    }

}
