package com.oheers.fish.competition.types;

import com.oheers.fish.api.Logging;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import net.kyori.adventure.text.Component;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class SpecificFishCompetitionType implements CompetitionType {

    @Override
    public @NonNull Component getTypeVariable() {
        return ConfigMessage.COMPETITION_TYPE_SPECIFIC.getMessage().getComponentMessage();
    }

    @Override
    public @NonNull Component getBossbarPrefix() {
        return Component.text("Specific Fish");
    }

    @Override
    public boolean shouldReverseLeaderboard() {
        return false;
    }

    @Override
    public boolean isUsable(@NonNull Competition competition) {
        if (!competition.chooseFish()) {
            Logging.warn("Failed to select a fish for Competition " + competition.getCompetitionName());
            return false;
        }
        if (competition.getNumberNeeded() <= 0) {
            Logging.warn("Competition " + competition.getCompetitionName() + " does not have number-needed set. Defaulting to 1.");
            competition.setNumberNeeded(1);
        }
        return true;
    }

    @Override
    public void applyToLeaderboard(@NonNull IFish fish, @NonNull UUID fisher, @NonNull Leaderboard leaderboard, @NonNull Competition competition) {
        IFish selected = competition.getSelectedFish();
        if (!fish.equals(selected)) {
            return;
        }
        CompetitionEntry entry = leaderboard.getEntry(fisher);
        if (entry == null) {
            entry = new CompetitionEntry(fisher, fish, this);
            leaderboard.addEntry(entry);
        } else {
            entry = leaderboard.trackFish(entry, fish);
        }
        if (entry.getValue() >= competition.getNumberNeeded()) {
            competition.setSingleWinner(fisher);
            competition.end(false);
        }
    }

    @Override
    public @NonNull Component formatLeaderboardEntry(@NonNull CompetitionEntry entry) {
        EMFMessage message = ConfigMessage.LEADERBOARD_MOST_FISH.getMessage();
        message.setAmount((int) entry.getValue());
        return message.getComponentMessage();
    }

    @Override
    public boolean useFishLength() {
        return false;
    }

    @Override
    public boolean isSingleReward() {
        return true;
    }

    @Override
    public @NonNull String getKey() {
        return "specific_fish";
    }
}
