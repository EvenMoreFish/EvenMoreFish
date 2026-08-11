package com.oheers.fish.database.data;

import com.oheers.fish.api.fishing.items.IFish;
import org.jetbrains.annotations.Contract;
import org.jspecify.annotations.NonNull;

//For use with caching only
public record FishRarityKey(@NonNull String fishName, @NonNull String fishRarity) {


    @Contract(value = "_, _ -> new", pure = true)
    public static @NonNull FishRarityKey of(final String fishName, final String fishRarity) {
        return new FishRarityKey(fishName, fishRarity);
    }

    public static @NonNull FishRarityKey from(final String pattern) {
        if (pattern == null || pattern.isEmpty())
            return empty();

        int separatorIndex = pattern.lastIndexOf('.');
        if (separatorIndex <= 0 || separatorIndex == pattern.length() - 1) {
            return empty();
        }

        return new FishRarityKey(
            pattern.substring(0, separatorIndex),
            pattern.substring(separatorIndex + 1)
        );
    }

    public static @NonNull FishRarityKey of(final @NonNull IFish fish) {
        return new FishRarityKey(fish.getName(), fish.getRarity().getId());
    }

    public static @NonNull FishRarityKey empty() {
        return new FishRarityKey("", "");
    }

    @Override
    public @NonNull String toString() {
        return fishName + "." + fishRarity;
    }

    public String toStringDefault() {
        return "FishRarityKey{" +
            "fishName='" + fishName + '\'' +
            ", fishRarity='" + fishRarity + '\'' +
            '}';
    }

}
