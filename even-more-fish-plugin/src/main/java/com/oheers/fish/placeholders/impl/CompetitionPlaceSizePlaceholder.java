package com.oheers.fish.placeholders.impl;

import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.placeholders.EMFPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompetitionPlaceSizePlaceholder implements EMFPlaceholder {

    private static final int PREFIX_LENGTH = "competition_place_size_".length();

    @Override
    public boolean shouldProcess(@NotNull String identifier) {
        return identifier.startsWith("competition_place_size_");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NotNull String identifier) {
        Competition activeComp = Competition.getCurrentlyActive();
        if (activeComp == null) {
            return ConfigMessage.PLACEHOLDER_NO_COMPETITION_RUNNING_SIZE.getMessage().getLegacyMessage();
        }

        if (!activeComp.getCompetitionType().getStrategy().shouldUseFishLength()) {
            return ConfigMessage.PLACEHOLDER_SIZE_DURING_MOST_FISH.getMessage().getLegacyMessage();
        }

        CompetitionEntry entry = fetchEntry(activeComp, identifier, PREFIX_LENGTH, ConfigMessage.PLACEHOLDER_NO_SIZE_IN_PLACE);
        if (entry == null) {
            return null;
        }
        float value = entry.getValue();
        if (value <= 0) {
            return null;
        }

        return String.valueOf(FishUtils.roundDouble(value, 1));
    }

}
