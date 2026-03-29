package com.oheers.fish.placeholders.impl.database;

import com.oheers.fish.api.Logging;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.placeholders.EMFPlaceholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TotalMoneyEarnedPlaceholder implements EMFPlaceholder {

    private static final int PREFIX_LENGTH = "total_money_earned_".length();

    @Override
    public boolean shouldProcess(@NotNull String identifier) {
        return identifier.startsWith("total_money_earned_");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable Player player, @NotNull String identifier) {
        UUID uuid = fetchPlayerOrUUIDString(player, identifier.substring(PREFIX_LENGTH));
        if (uuid == null) {
            Logging.debug("Could not resolve UUID from placeholder: " + identifier);
            return null;
        }
        UserReport report = fetchUserReport(uuid);
        if (report == null) {
            Logging.debug("Could not find user report for placeholder: " + identifier + " with UUID: " + uuid);
            return format(0D);
        }
        return format(report.getMoneyEarned());
    }

    private String format(double value) {
        return String.format("%.2f", value);
    }

}
