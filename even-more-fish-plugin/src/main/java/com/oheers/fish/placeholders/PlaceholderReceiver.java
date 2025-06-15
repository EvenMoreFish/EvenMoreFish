package com.oheers.fish.placeholders;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.database.model.user.UserReport;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**
 * Handles all PlaceholderAPI expansions for EvenMoreFish.
 */
public class PlaceholderReceiver extends PlaceholderExpansion {

    private static final Map<CompetitionType, ConfigMessage> COMPETITION_TYPE_MESSAGES = Map.of(
            CompetitionType.LARGEST_FISH, ConfigMessage.COMPETITION_TYPE_LARGEST,
            CompetitionType.LARGEST_TOTAL, ConfigMessage.COMPETITION_TYPE_LARGEST_TOTAL,
            CompetitionType.MOST_FISH, ConfigMessage.COMPETITION_TYPE_MOST,
            CompetitionType.SPECIFIC_FISH, ConfigMessage.COMPETITION_TYPE_SPECIFIC,
            CompetitionType.SPECIFIC_RARITY, ConfigMessage.COMPETITION_TYPE_SPECIFIC_RARITY,
            CompetitionType.SHORTEST_FISH, ConfigMessage.COMPETITION_TYPE_SHORTEST,
            CompetitionType.SHORTEST_TOTAL, ConfigMessage.COMPETITION_TYPE_SHORTEST_TOTAL
    );

    private final EvenMoreFish plugin;
    private final List<HandlerDefinition> handlers;

    public PlaceholderReceiver(EvenMoreFish plugin) {
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

    @Override
    public @NotNull String getAuthor() {
        return plugin.getPluginMeta().getAuthors().toString();
    }

    @Override
    public @NotNull String getIdentifier() {
        return "emf";
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    private List<HandlerDefinition> createHandlers() {
        return Stream.of(
                        // Competition placeholders
                        new HandlerDefinition(
                                id -> id.startsWith("competition_place_size_"),
                                Priority.SPECIFIC_PATTERN,
                                this::handleCompetitionPlaceSize
                        ),
                        new HandlerDefinition(
                                id -> id.startsWith("competition_place_fish_"),
                                Priority.SPECIFIC_PATTERN,
                                this::handleCompetitionPlaceFish
                        ),
                        new HandlerDefinition(
                                id -> id.startsWith("competition_place_player_"),
                                Priority.SPECIFIC_PATTERN,
                                this::handleCompetitionPlacePlayer
                        ),

                        // Total stats placeholders
                        new HandlerDefinition(
                                id -> id.startsWith("total_money_earned_"),
                                Priority.GENERAL_PATTERN,
                                this::handleTotalMoneyEarned
                        ),
                        new HandlerDefinition(
                                id -> id.startsWith("total_fish_sold_"),
                                Priority.GENERAL_PATTERN,
                                this::handleTotalFishSold
                        ),

                        // Exact match placeholders
                        new HandlerDefinition(
                                id -> id.equalsIgnoreCase("competition_time_left"),
                                Priority.EXACT_MATCH,
                                this::handleCompetitionTimeLeft
                        ),
                        new HandlerDefinition(
                                id -> id.equalsIgnoreCase("competition_active"),
                                Priority.EXACT_MATCH,
                                this::handleCompetitionActive
                        ),
                        new HandlerDefinition(
                                id -> id.equalsIgnoreCase("custom_fishing_boolean"),
                                Priority.EXACT_MATCH,
                                this::handleCustomFishingBoolean
                        ),
                        new HandlerDefinition(
                                id -> id.equalsIgnoreCase("custom_fishing_status"),
                                Priority.EXACT_MATCH,
                                this::handleCustomFishingStatus
                        ),
                        new HandlerDefinition(
                                id -> id.equalsIgnoreCase("competition_type"),
                                Priority.EXACT_MATCH,
                                this::handleCompetitionType
                        ),
                        new HandlerDefinition(
                                id -> id.equalsIgnoreCase("competition_type_format"),
                                Priority.EXACT_MATCH,
                                this::handleCompetitionTypeFormat
                        )
                )
                .sorted(Comparator.comparingInt(def -> def.priority().ordinal()))
                .toList();
    }

    private String handleCompetitionPlacePlayer(Player player, String identifier) {
        Competition activeComp = Competition.getCurrentlyActive();
        if (activeComp == null) {
            return ConfigMessage.PLACEHOLDER_NO_COMPETITION_RUNNING.getMessage().getLegacyMessage();
        }

        int place = parsePlace(identifier, "competition_place_player_".length());
        if (!leaderboardContainsPlace(activeComp, place)) {
            return ConfigMessage.PLACEHOLDER_NO_PLAYER_IN_PLACE.getMessage().getLegacyMessage();
        }

        UUID uuid = activeComp.getLeaderboard().getEntry(place).getPlayer();
        if (uuid != null) {
            OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(uuid);
            return offlinePlayer.getName();
        }
        return "";
    }

    private @NotNull String handleCompetitionTypeFormat(Player player, String identifier) {
        Competition activeComp = Competition.getCurrentlyActive();
        if (activeComp == null) {
            return "";
        }

        ConfigMessage message = COMPETITION_TYPE_MESSAGES.get(activeComp.getCompetitionType());
        return message != null ? message.getMessage().getLegacyMessage() : "";
    }

    private String handleCompetitionType(Player player, String identifier) {
        Competition activeComp = Competition.getCurrentlyActive();
        return activeComp != null
                ? activeComp.getCompetitionType().name()
                : ConfigMessage.PLACEHOLDER_NO_COMPETITION_RUNNING.getMessage().getLegacyMessage();
    }

    private String handleCompetitionPlaceSize(Player player, String identifier) {
        Competition activeComp = Competition.getCurrentlyActive();
        if (activeComp == null) {
            return ConfigMessage.PLACEHOLDER_NO_COMPETITION_RUNNING_SIZE.getMessage().getLegacyMessage();
        }

        if (!isSizeCompetition(activeComp.getCompetitionType())) {
            return ConfigMessage.PLACEHOLDER_SIZE_DURING_MOST_FISH.getMessage().getLegacyMessage();
        }

        int place = parsePlace(identifier, "competition_place_size_".length());
        if (!leaderboardContainsPlace(activeComp, place)) {
            return ConfigMessage.PLACEHOLDER_NO_SIZE_IN_PLACE.getMessage().getLegacyMessage();
        }

        float value = activeComp.getLeaderboard().getEntry(place).getValue();
        return value != -1.0f ? Double.toString(FishUtils.roundDouble(value, 1)) : "";
    }

    private String handleCompetitionPlaceFish(Player player, String identifier) {
        Competition activeComp = Competition.getCurrentlyActive();
        if (activeComp == null) {
            return ConfigMessage.PLACEHOLDER_NO_COMPETITION_RUNNING_FISH.getMessage().getLegacyMessage();
        }

        int place = parsePlace(identifier, "competition_place_fish_".length());

        if (activeComp.getCompetitionType() == CompetitionType.LARGEST_FISH) {
            if (!leaderboardContainsPlace(activeComp, place)) {
                return ConfigMessage.PLACEHOLDER_NO_FISH_IN_PLACE.getMessage().getLegacyMessage();
            }

            Fish fish = activeComp.getLeaderboard().getEntry(place).getFish();
            if (fish != null) {
                return formatFishMessage(fish);
            }
        } else {
            float value = activeComp.getLeaderboard().getEntry(place).getValue();
            if (value != -1) {
                return formatMostFishMessage((int) value);
            }
            return ConfigMessage.PLACEHOLDER_NO_FISH_IN_PLACE.getMessage().getLegacyMessage();
        }
        return "";
    }

    private @Nullable String handleTotalMoneyEarned(Player player, @NotNull String identifier) {
        try {
            UUID uuid = UUID.fromString(identifier.substring("total_money_earned_".length()));
            UserReport userReport = plugin.getUserReportDataManager().get(uuid.toString());
            return userReport != null ? String.format("%.2f", userReport.getMoneyEarned()) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private @Nullable String handleTotalFishSold(Player player, @NotNull String identifier) {
        try {
            UUID uuid = UUID.fromString(identifier.substring("total_fish_sold_".length()));
            UserReport userReport = plugin.getUserReportDataManager().get(uuid.toString());
            return userReport != null ? String.valueOf(userReport.getFishSold()) : null;
        } catch (IllegalArgumentException e) {
            return null;
        }
    }

    private @NotNull String handleCompetitionTimeLeft(Player player, String identifier) {
        return Competition.getNextCompetitionMessage().getLegacyMessage();
    }

    private @NotNull String handleCompetitionActive(Player player, String identifier) {
        return Boolean.toString(Competition.isActive());
    }

    private @NotNull String handleCustomFishingBoolean(Player player, String identifier) {
        return Boolean.toString(!plugin.isCustomFishingDisabled(player));
    }

    private @NotNull String handleCustomFishingStatus(Player player, String identifier) {
        return plugin.isCustomFishingDisabled(player)
                ? ConfigMessage.CUSTOM_FISHING_DISABLED.getMessage().getLegacyMessage()
                : ConfigMessage.CUSTOM_FISHING_ENABLED.getMessage().getLegacyMessage();
    }

    /* Helper Methods */
    private int parsePlace(String identifier, int prefixLength) {
        try {
            return Integer.parseInt(identifier.substring(prefixLength));
        } catch (NumberFormatException e) {
            return -1;
        }
    }

    private boolean isSizeCompetition(CompetitionType type) {
        return type == CompetitionType.LARGEST_FISH || type == CompetitionType.LARGEST_TOTAL;
    }

    private @NotNull String formatFishMessage(@NotNull Fish fish) {
        EMFMessage message = fish.getLength() == -1
                ? ConfigMessage.PLACEHOLDER_FISH_LENGTHLESS_FORMAT.getMessage()
                : ConfigMessage.PLACEHOLDER_FISH_FORMAT.getMessage();

        message.setLength(Float.toString(fish.getLength()));
        message.setFishCaught(fish.getDisplayName());
        message.setRarity(fish.getRarity().getDisplayName());
        return message.getLegacyMessage();
    }

    private @NotNull String formatMostFishMessage(int amount) {
        EMFMessage message = ConfigMessage.PLACEHOLDER_FISH_MOST_FORMAT.getMessage();
        message.setAmount(amount);
        return message.getLegacyMessage();
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) return "";

        for (HandlerDefinition def : handlers) {
            if (def.matcher().test(identifier)) {
                return def.handler().apply(player, identifier);
            }
        }
        return null;
    }

    private record HandlerDefinition(
            Predicate<String> matcher,
            Priority priority,
            BiFunction<Player, String, String> handler
    ) {}

    private boolean leaderboardContainsPlace(@NotNull Competition competition, int place) {
        return place > 0 && competition.getLeaderboardSize() >= place;
    }
}