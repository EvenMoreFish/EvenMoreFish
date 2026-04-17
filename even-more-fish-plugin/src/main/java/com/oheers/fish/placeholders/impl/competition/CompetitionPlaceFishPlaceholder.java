package com.oheers.fish.placeholders.impl.competition;

import com.oheers.fish.competition.Competition;
import com.oheers.fish.competition.CompetitionEntry;
import com.oheers.fish.competition.CompetitionType;
import com.oheers.fish.fishing.items.Fish;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.placeholders.abstracted.EMFPlaceholder;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class CompetitionPlaceFishPlaceholder implements EMFPlaceholder {

    private static final int PREFIX_LENGTH = "competition_place_fish_".length();

    @Override
    public boolean shouldProcess(@NotNull String identifier) {
        return identifier.startsWith("competition_place_fish_");
    }

    @Override
    public @Nullable String parsePAPI(@Nullable Player player, @NotNull String identifier) {
        Competition activeComp = Competition.getCurrentlyActive();
        if (activeComp == null) {
            return ConfigMessage.PLACEHOLDER_NO_COMPETITION_RUNNING_FISH.getMessage().getLegacyMessage();
        }

        CompetitionEntry entry = fetchEntry(activeComp, identifier, PREFIX_LENGTH);
        if (entry == null) {
            return ConfigMessage.PLACEHOLDER_NO_FISH_IN_PLACE.getMessage().getLegacyMessage();
        }

        if (activeComp.getCompetitionType() == CompetitionType.LARGEST_FISH) {
            return formatFishMessage(entry.getFish());
        } else {
            float value = entry.getValue();
            if (value <= 0) {
                return ConfigMessage.PLACEHOLDER_NO_FISH_IN_PLACE.getMessage().getLegacyMessage();
            }
            return formatMostFishMessage((int) value);
        }
    }

    private @Nullable String formatFishMessage(@Nullable Fish fish) {
        if (fish == null) {
            return null;
        }
        EMFMessage message = fish.getLength() == -1
            ? ConfigMessage.PLACEHOLDER_FISH_LENGTHLESS_FORMAT.getMessage()
            : ConfigMessage.PLACEHOLDER_FISH_FORMAT.getMessage();

        message.setLength(Float.toString(fish.getLength()));
        message.setFishCaught(fish.getDisplayName());
        message.setRarity(fish.getRarity());
        return message.getLegacyMessage();
    }

    private @NotNull String formatMostFishMessage(int amount) {
        EMFMessage message = ConfigMessage.PLACEHOLDER_FISH_MOST_FORMAT.getMessage();
        message.setAmount(amount);
        return message.getLegacyMessage();
    }

}
