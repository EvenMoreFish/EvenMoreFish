package com.oheers.fish.placeholders.impl.competition;

import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionManager;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.placeholders.abstracted.EMFPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class CompetitionPlaceSizePlaceholder implements EMFPlaceholder {

    private static final int PREFIX_LENGTH = "competition_place_size_".length();

    @Override
    public boolean shouldProcess(@NonNull String identifier) {
        return identifier.startsWith("competition_place_size_");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NonNull String identifier) {
        Competition activeComp = CompetitionManager.getInstance().getActiveCompetition();
        if (activeComp == null || activeComp.getCompetitionType() == null) {
            return ConfigMessage.PLACEHOLDER_NO_COMPETITION_RUNNING_SIZE.getMessage().getLegacyMessage(null);
        }

        if (!activeComp.getCompetitionType().useFishLength()) {
            return ConfigMessage.PLACEHOLDER_SIZE_DURING_MOST_FISH.getMessage().getLegacyMessage(null);
        }

        CompetitionEntry entry = fetchEntry(activeComp, identifier, PREFIX_LENGTH);
        if (entry == null) {
            return ConfigMessage.PLACEHOLDER_NO_SIZE_IN_PLACE.getMessage().getLegacyMessage(null);
        }
        return String.valueOf(FishUtils.roundDouble(entry.getValue(), 1));
    }

}
