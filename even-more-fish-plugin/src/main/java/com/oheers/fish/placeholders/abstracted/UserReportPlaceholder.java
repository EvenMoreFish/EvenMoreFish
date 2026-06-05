package com.oheers.fish.placeholders.abstracted;

import com.oheers.fish.api.Logging;
import com.oheers.fish.database.model.user.UserReport;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.ApiStatus;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;
import java.util.function.Function;

@ApiStatus.Internal
public abstract class UserReportPlaceholder implements EMFPlaceholder {

    private final String prefix;
    private final int prefixLength;
    private final Function<UserReport, String> func;
    private final String def;

    public UserReportPlaceholder(@NotNull String prefix, @NotNull Function<@NotNull UserReport, @NotNull String> func, @NotNull String def) {
        this.prefix = prefix;
        this.prefixLength = prefix.length();
        this.func = func;
        this.def = def;
    }

    @Override
    public boolean shouldProcess(@NotNull String identifier) {
        return identifier.startsWith(prefix);
    }

    @Override
    public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NotNull String identifier) {
        String target = identifier.substring(prefixLength);
        UUID uuid = fetchPlayerOrUUIDString(player, target);
        if (uuid == null) {
            if (target.equalsIgnoreCase("player")) {
                Logging.debug("Placeholder %s could not resolve player context for identifier: %s".formatted(getClass().getSimpleName(), identifier));
            } else {
                Logging.debug("Placeholder %s received an invalid UUID target '%s' for identifier: %s".formatted(getClass().getSimpleName(), target, identifier));
            }
            return null;
        }
        UserReport report = fetchUserReport(uuid);
        if (report == null) {
            Logging.debug("Placeholder %s could not find user report for identifier: %s with UUID: %s".formatted(getClass().getSimpleName(), identifier, uuid));
            return def;
        }
        return this.func.apply(report);
    }

}
