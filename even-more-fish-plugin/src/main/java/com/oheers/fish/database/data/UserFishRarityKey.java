package com.oheers.fish.database.data;

import com.oheers.fish.fishing.items.Fish;
import org.jetbrains.annotations.NotNull;
import org.jspecify.annotations.NonNull;

public record UserFishRarityKey(int userId, @NotNull String fishName, @NotNull String fishRarity) {

    public static @NotNull UserFishRarityKey of(final int userId, final String fishName, final String fishRarity) {
        return new UserFishRarityKey(userId, fishName, fishRarity);
    }

    public static @NotNull UserFishRarityKey of(final int userId, final @NotNull Fish fish) {
        return new UserFishRarityKey(userId, fish.getName(), fish.getRarity().getId());
    }

    public static @NotNull UserFishRarityKey from(final @NotNull String pattern) {
        int firstSeparator = pattern.indexOf('.');
        int lastSeparator = pattern.lastIndexOf('.');
        if (firstSeparator <= 0 || lastSeparator <= firstSeparator || lastSeparator == pattern.length() - 1) {
            return empty();
        }

        try {
            int userId = Integer.parseInt(pattern.substring(0, firstSeparator));
            String fishName = pattern.substring(firstSeparator + 1, lastSeparator);
            String fishRarity = pattern.substring(lastSeparator + 1);
            return new UserFishRarityKey(userId, fishName, fishRarity);
        } catch (NumberFormatException e) {
            return empty();
        }
    }

    public static @NotNull UserFishRarityKey empty() {
        return new UserFishRarityKey(-1, "", "");
    }

    @Override
    public @NotNull String toString() {
        return userId + "." + fishName + "." + fishRarity;
    }

    public String toStringDefault() {
        return "UserFishRarityKey{" +
            "userId='" + userId + '\'' +
            "fishName='" + fishName + '\'' +
            ", fishRarity='" + fishRarity + '\'' +
            '}';
    }
}
