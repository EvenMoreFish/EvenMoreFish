package com.oheers.fish.placeholders.impl.competition;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.placeholders.abstracted.EMFPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CompetitionTypePlaceholder implements EMFPlaceholder {

    @Override
    public boolean shouldProcess(@NonNull String identifier) {
        return identifier.equalsIgnoreCase("competition_type");
    }

    @Override
    public @NonNull String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier) {
        Competition activeComp = Competition.getCurrentlyActive();
        if (activeComp == null || activeComp.getCompetitionType() == null) {
            return ConfigMessage.PLACEHOLDER_NO_COMPETITION_RUNNING.getMessage().getLegacyMessage(null);
        }
        return activeComp.getCompetitionType().getKey();
    }

}
