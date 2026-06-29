package com.oheers.fish.placeholders.impl.competition;

import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.placeholders.abstracted.EMFPlaceholder;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompetitionTimeLeftPlaceholder implements EMFPlaceholder {

    @Override
    public boolean shouldProcess(@NotNull String identifier) {
        return identifier.equalsIgnoreCase("competition_time_left");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable OfflinePlayer player, @NotNull String identifier) {
        Competition competition = Competition.getCurrentlyActive();
        if (competition == null) {
            return Competition.getNextCompetitionMessage().getLegacyMessage(null);
        }
        EMFMessage message = ConfigMessage.PLACEHOLDER_TIME_REMAINING_ACTIVE.getMessage();
        message.setVariable("{time-left}", FishUtils.timeFormat(competition.getTimeLeft()));
        return message.getLegacyMessage(null);
    }

}
