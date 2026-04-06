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
        String[] parts = pattern.split("\\.");
        if (parts.length != 3) {
            return empty();
        }

        try {
            int userId = Integer.parseInt(parts[0]);
            String fishName = parts[1];
            String fishRarity = parts[2];
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