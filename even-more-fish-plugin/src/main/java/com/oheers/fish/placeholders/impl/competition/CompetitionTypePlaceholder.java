package com.oheers.fish.placeholders.impl.competition;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.placeholders.abstracted.EMFPlaceholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompetitionTypePlaceholder implements EMFPlaceholder {

    @Override
    public boolean shouldProcess(@NotNull String identifier) {
        return identifier.equalsIgnoreCase("competition_type");
    }

    @Override
    public @NotNull String parsePAPI(@Nullable Player player, @NotNull String identifier) {
        Competition activeComp = Competition.getCurrentlyActive();
        if (activeComp == null) {
            return ConfigMessage.PLACEHOLDER_NO_COMPETITION_RUNNING.getMessage().getLegacyMessage();
        }
        return activeComp.getCompetitionType().toString();
    }

}
