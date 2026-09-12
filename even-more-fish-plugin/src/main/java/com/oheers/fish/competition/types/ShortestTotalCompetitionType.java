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

public class ShortestTotalCompetitionType implements CompetitionType {

    @Override
    public @NonNull Component getTypeVariable() {
        return ConfigMessage.COMPETITION_TYPE_SHORTEST_TOTAL.getMessage().getComponentMessage();
    }

    @Override
    public @NonNull Component getBossbarPrefix() {
        return Component.text("Shortest Total");
    }

    @Override
    public boolean shouldReverseLeaderboard() {
        return true;
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
        if (entry != null) {
            leaderboard.trackFish(entry, fish);
        } else {
            leaderboard.addEntry(new CompetitionEntry(fisher, fish, this));
        }
    }

    @Override
    public @NonNull Component formatLeaderboardEntry(@NonNull CompetitionEntry entry) {
        EMFMessage message = ConfigMessage.LEADERBOARD_SHORTEST_TOTAL.getMessage();
        message.setAmount(FishUtils.roundFloat(entry.getValue(), 1));
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
        return "shortest_total";
    }

}
