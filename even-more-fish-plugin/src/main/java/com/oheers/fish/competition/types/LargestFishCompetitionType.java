package com.oheers.fish.competition.types;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.fishing.items.IFish;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import net.kyori.adventure.text.Component;
import org.bukkit.plugin.Plugin;
import org.jspecify.annotations.NonNull;

import java.util.UUID;

public class LargestFishCompetitionType implements CompetitionType {

    @Override
    public @NonNull Component getTypeVariable() {
        return ConfigMessage.COMPETITION_TYPE_LARGEST.getMessage().getComponentMessage();
    }

    @Override
    public @NonNull Component getBossbarPrefix() {
        return Component.text("Largest Fish");
    }

    @Override
    public boolean shouldReverseLeaderboard() {
        return false;
    }

    @Override
    public boolean isUsable(@NonNull Competition competition) {
        return true;
    }

    @Override
    public void applyToLeaderboard(@NonNull IFish fish, @NonNull UUID fisher, @NonNull Leaderboard leaderboard, @NonNull Competition competition) {
        if (fish.isLengthless()) {
            return;
        }
        CompetitionEntry entry = leaderboard.getEntry(fisher);
        if (entry == null) {
            leaderboard.addEntry(new CompetitionEntry(fisher, fish, this));
            return;
        }
        if (fish.getLength() > entry.getFish().getLength()) {
            // These HAVE to be in this order, otherwise players are spammed with new first place messages
            leaderboard.addEntry(new CompetitionEntry(
                fisher,
                fish,
                competition.getCompetitionType()
            ));
            leaderboard.removeEntry(entry);
        }
    }

    @Override
    public @NonNull Component formatLeaderboardEntry(@NonNull CompetitionEntry entry) {
        EMFMessage message = ConfigMessage.LEADERBOARD_LARGEST_FISH.getMessage();
        message.setLength(FishUtils.roundFloat(entry.getValue(), 1));
        return message.getComponentMessage();
    }

    @Override
    public boolean useFishLength() {
        return true;
    }

    @Override
    public boolean isSingleReward() {
        return false;
    }

    @Override
    public @NonNull String getAuthor() {
        return "FireML";
    }

    @Override
    public @NonNull Plugin getPlugin() {
        return EvenMoreFish.getInstance();
    }

    @Override
    public @NonNull String getKey() {
        return "largest_fish";
    }
}
