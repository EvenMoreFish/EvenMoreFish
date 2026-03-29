package com.oheers.fish.placeholders.impl.competition;

import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.placeholders.abstracted.EMFPlaceholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompetitionPlacePlayerPlaceholder implements EMFPlaceholder {

    private static final int PREFIX_LENGTH = "competition_place_player_".length();

    @Override
    public boolean shouldProcess(@NotNull String identifier) {
        return identifier.startsWith("competition_place_player_");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable Player player, @NotNull String identifier) {
        Competition activeComp = Competition.getCurrentlyActive();
        if (activeComp == null) {
            return ConfigMessage.PLACEHOLDER_NO_COMPETITION_RUNNING.getMessage().getLegacyMessage();
        }
        CompetitionEntry entry = fetchEntry(activeComp, identifier, PREFIX_LENGTH);
        if (entry == null) {
            return ConfigMessage.PLACEHOLDER_NO_PLAYER_IN_PLACE.getMessage().getLegacyMessage();
        }
        return FishUtils.getPlayerName(entry.getPlayer());
    }

}
