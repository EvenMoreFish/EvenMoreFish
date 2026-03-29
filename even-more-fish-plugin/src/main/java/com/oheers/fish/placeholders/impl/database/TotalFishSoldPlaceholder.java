package com.oheers.fish.placeholders.impl.database;

import com.oheers.fish.api.Logging;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.placeholders.EMFPlaceholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class TotalFishSoldPlaceholder implements EMFPlaceholder {

    private static final int PREFIX_LENGTH = "total_fish_sold_".length();

    @Override
    public boolean shouldProcess(@NotNull String identifier) {
        return identifier.startsWith("total_fish_sold_");
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
            return "0";
        }
        return String.valueOf(report.getFishSold());
    }

}
