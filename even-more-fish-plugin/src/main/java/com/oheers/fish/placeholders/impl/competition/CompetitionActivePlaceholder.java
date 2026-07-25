package com.oheers.fish.placeholders.impl.competition;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.placeholders.abstracted.EMFPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CompetitionActivePlaceholder implements EMFPlaceholder {

    @Override
    public boolean shouldProcess(@NonNull String identifier) {
        return identifier.equalsIgnoreCase("competition_active");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier) {
        return String.valueOf(Competition.isActive());
    }

}
