package com.oheers.fish.placeholders;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.placeholders.abstracted.EMFPlaceholder;
import com.oheers.fish.placeholders.impl.competition.CompetitionActivePlaceholder;
import com.oheers.fish.placeholders.impl.competition.CompetitionPlaceFishPlaceholder;
import com.oheers.fish.placeholders.impl.competition.CompetitionPlacePlayerPlaceholder;
import com.oheers.fish.placeholders.impl.competition.CompetitionPlaceSizePlaceholder;
import com.oheers.fish.placeholders.impl.competition.CompetitionTimeLeftPlaceholder;
import com.oheers.fish.placeholders.impl.competition.CompetitionTypeFormatPlaceholder;
import com.oheers.fish.placeholders.impl.competition.CompetitionTypePlaceholder;
import com.oheers.fish.placeholders.impl.database.player.DistinctFishCaughtInRarityPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.FishCaughtOutOfRarityPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.FishCaughtOutOfTotalPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.FirstUncaughtFishPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.HasCaughtFishPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.HasCompletedCollectionPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.HasCompletedRarityPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.PercentCaughtInRarityPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.PercentCaughtTotalPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.RemainingFishInRarityPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.RemainingFishTotalPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TimesCaughtFishPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TotalCompetitionsJoinedPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TotalCompetitionsWonPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TotalFishCaughtPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TotalFishCaughtInRarityPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TotalFishSoldPlaceholder;
import com.oheers.fish.placeholders.impl.database.player.TotalMoneyEarnedPlaceholder;
import com.oheers.fish.placeholders.impl.player.CustomFishingBooleanPlaceholder;
import com.oheers.fish.placeholders.impl.player.CustomFishingStatusPlaceholder;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.OfflinePlayer;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.Stream;

/**
 * Handles all PlaceholderAPI expansions for EvenMoreFish.
 */
public class PlaceholderReceiver extends PlaceholderExpansion {

    private final EvenMoreFish plugin;
    private final List<EMFPlaceholder> handlers;

    public PlaceholderReceiver(@NotNull EvenMoreFish plugin) {
        this.plugin = plugin;
        this.handlers = createHandlers();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull String getAuthor() {
        return plugin.getPluginMeta().getAuthors().toString();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "emf";
    }

    @SuppressWarnings("UnstableApiUsage")
    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    private List<EMFPlaceholder> createHandlers() {
        return Stream.of(
            // Competition
            new CompetitionActivePlaceholder(),
            new CompetitionPlaceFishPlaceholder(),
            new CompetitionPlacePlayerPlaceholder(),
            new CompetitionPlaceSizePlaceholder(),
            new CompetitionTimeLeftPlaceholder(),
            new CompetitionTypeFormatPlaceholder(),
            new CompetitionTypePlaceholder(),

            // Database - Player
            new DistinctFishCaughtInRarityPlaceholder(),
            new FishCaughtOutOfRarityPlaceholder(),
            new FishCaughtOutOfTotalPlaceholder(),
            new FirstUncaughtFishPlaceholder(),
            new HasCaughtFishPlaceholder(),
            new HasCompletedCollectionPlaceholder(),
            new HasCompletedRarityPlaceholder(),
            new PercentCaughtInRarityPlaceholder(),
            new PercentCaughtTotalPlaceholder(),
            new RemainingFishInRarityPlaceholder(),
            new RemainingFishTotalPlaceholder(),
            new TimesCaughtFishPlaceholder(),
            new TotalCompetitionsJoinedPlaceholder(),
            new TotalCompetitionsWonPlaceholder(),
            new TotalFishCaughtPlaceholder(),
            new TotalFishCaughtInRarityPlaceholder(),
            new TotalFishSoldPlaceholder(),
            new TotalMoneyEarnedPlaceholder(),

            // Player
            new CustomFishingBooleanPlaceholder(),
            new CustomFishingStatusPlaceholder()
        ).toList();
    }

    @Override
    public @Nullable String onRequest(@Nullable OfflinePlayer player, @NotNull final String identifier) {
        for (EMFPlaceholder handler : handlers) {
            if (handler.shouldProcess(identifier)) {
                return handler.parsePAPI(player, identifier);
            }
        }
        return null;
    }

}
