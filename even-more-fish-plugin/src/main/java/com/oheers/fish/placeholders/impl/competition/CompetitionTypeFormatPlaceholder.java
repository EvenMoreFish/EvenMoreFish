package com.oheers.fish.placeholders.impl.competition;

import com.oheers.fish.api.Logging;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.placeholders.EMFPlaceholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;

public class CompetitionTypeFormatPlaceholder implements EMFPlaceholder {

    private static final Map<CompetitionType, ConfigMessage> COMPETITION_TYPE_MESSAGES = Map.of(
        CompetitionType.LARGEST_FISH, ConfigMessage.COMPETITION_TYPE_LARGEST,
        CompetitionType.LARGEST_TOTAL, ConfigMessage.COMPETITION_TYPE_LARGEST_TOTAL,
        CompetitionType.MOST_FISH, ConfigMessage.COMPETITION_TYPE_MOST,
        CompetitionType.SPECIFIC_FISH, ConfigMessage.COMPETITION_TYPE_SPECIFIC,
        CompetitionType.SPECIFIC_RARITY, ConfigMessage.COMPETITION_TYPE_SPECIFIC_RARITY,
        CompetitionType.SHORTEST_FISH, ConfigMessage.COMPETITION_TYPE_SHORTEST,
        CompetitionType.SHORTEST_TOTAL, ConfigMessage.COMPETITION_TYPE_SHORTEST_TOTAL
    );

    @Override
    public boolean shouldProcess(@NotNull String identifier) {
        return identifier.equalsIgnoreCase("competition_type_format");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable Player player, @NotNull String identifier) {
        Competition activeComp = Competition.getCurrentlyActive();
        if (activeComp == null) {
            return ConfigMessage.NO_COMPETITION_RUNNING.getMessage().getLegacyMessage();
        }

        CompetitionType type = activeComp.getCompetitionType();
        ConfigMessage message = COMPETITION_TYPE_MESSAGES.get(type);
        if (message == null) {
            Logging.debug("Could not find message for CompetitionType: " + type);
            return null;
        }

        EMFMessage typeFormat = activeComp.getCompetitionType().getStrategy().getTypeFormat(activeComp, message);
        return typeFormat.getLegacyMessage();
    }

}
