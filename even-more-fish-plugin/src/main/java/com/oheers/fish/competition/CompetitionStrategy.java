package com.oheers.fish.competition;

import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.leaderboard.Leaderboard;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.text.DecimalFormat;

/**
 * This interface defines the behavior for competition strategies.
 */
public interface CompetitionStrategy {

    /**
     * Begins the competition.
     *
     * @param competition The competition to begin.
     * @return True if the competition was successfully started, false otherwise.
     */
    default boolean begin(Competition competition) {
        return true;
    }

    /**
     * Applies the competition to the leaderboard.
     *
     * @param fish       The fish caught during the competition.
     * @param fisher     The player who caught the fish.
     * @param leaderboard The leaderboard to apply the competition to.
     * @param competition The competition being applied.
     */
    void applyToLeaderboard(Fish fish, Player fisher, Leaderboard leaderboard, Competition competition);

    /**
     * Gets the begin message for the competition.
     *
     * @param competition The competition to get the begin message for.
     * @param type        The type of competition.
     * @return The begin message for the competition.
     */
    default EMFMessage getBeginMessage(Competition competition, CompetitionType type) {
        EMFMessage message = ConfigMessage.COMPETITION_START.getMessage();
        message.setCompetitionType(type.getTypeVariable().getMessage());
        return message;
    }

    /**
     * Gets the single console leaderboard message.
     *
     * @param entry   The competition entry to get the leaderboard information from.
     * @return The single console leaderboard message.
     */
    EMFMessage getSingleConsoleLeaderboardMessage(@NotNull CompetitionEntry entry);

    /**
     * Gets the single player leaderboard message.
     *
     * @param entry   The competition entry to get the leaderboard information from.
     * @return The single player leaderboard message.
     */
    EMFMessage getSinglePlayerLeaderboard(@NotNull CompetitionEntry entry);

    /**
     * This creates a message object and applies all the settings to it to make it able to use the {type} variable. It
     * takes into consideration whether it's a specific fish/rarity competition.
     *
     * @param competition   The competition to get the message for.
     * @param configMessage The configmessage to use. Must have the {type} variable in it.
     * @return A message object that's pre-set to be compatible for the time remaining.
     */
    default @NotNull EMFMessage getTypeFormat(@NotNull Competition competition, ConfigMessage configMessage) {
        EMFMessage message = configMessage.getMessage();
        message.setTimeFormatted(FishUtils.timeFormat(competition.getTimeLeft()));
        message.setTimeRaw(FishUtils.timeRaw(competition.getTimeLeft()));
        message.setCompetitionType(competition.getCompetitionType().getTypeVariable().getMessage());
        return message;
    }

    default DecimalFormat getDecimalFormat() {
        return new DecimalFormat("#.0"); // For 1 decimal place
    }

}