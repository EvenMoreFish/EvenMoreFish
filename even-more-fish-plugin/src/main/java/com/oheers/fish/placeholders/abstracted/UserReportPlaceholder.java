package com.oheers.fish.placeholders.abstracted;

import com.oheers.fish.api.Logging;
import com.oheers.fish.database.model.user.UserReport;
import org.bukkit.entity.Player;
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
    public @Nullable String parsePAPI(@Nullable Player player, @NotNull String identifier) {
        UUID uuid = fetchPlayerOrUUIDString(player, identifier.substring(prefixLength));
        if (uuid == null) {
            Logging.debug("Could not resolve UUID from placeholder: " + identifier);
            return null;
        }
        UserReport report = fetchUserReport(uuid);
        if (report == null) {
            Logging.debug("Could not find user report for placeholder: " + identifier + " with UUID: " + uuid);
            return def;
        }
        return this.func.apply(report);
    }

}
