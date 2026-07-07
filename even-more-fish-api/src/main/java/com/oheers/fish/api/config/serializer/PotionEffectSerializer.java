package com.oheers.fish.api.config.serializer;

import com.oheers.fish.api.Logging;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class PotionEffectSerializer implements EMFSerializer<PotionEffect> {

    private static final PotionEffectSerializer INSTANCE = new PotionEffectSerializer();

    private PotionEffectSerializer() {}

    public static @NotNull PotionEffectSerializer get() {
        return INSTANCE;
    }

    public @NotNull String serialize(@NotNull PotionEffect element) {
        return element.getType().toString().toLowerCase() + "," + element.getAmplifier() + "," + element.getDuration();
    }

    public @Nullable PotionEffect deserialize(@Nullable String element) {
        if (element == null) {
            return null;
        }
        // Correct format using commas
        if (element.contains(",")) {
            return deserialize(element, ",");
        // Incorrect format that was shipped with default configs for a long time
        } else if (element.contains(":")) {
            return deserialize(element, ":");
        } else {
            return null;
        }
    }

    public @Nullable PotionEffect deserialize(@Nullable String element, @NotNull String separator) {
        if (element == null) {
            return null;
        }
        String[] split = element.split(separator);
        if (split.length != 3) {
            Logging.error("Invalid potion effect string: " + element);
            Logging.error("The correct format is \"potion,amplifier,duration\".");
            return null;
        }
        PotionEffectType type = PotionEffectType.getByName(split[0].toUpperCase());
        if (type == null) {
            Logging.error("Potion effect type " + split[0] + " is not valid.");
            return null;
        }
        int amplifier;
        try {
            amplifier = Integer.parseInt(split[1]);
        } catch (NumberFormatException exception) {
            Logging.error("Potion effect amplifier " + split[1] + " is not valid.");
            return null;
        }
        int duration;
        try {
            duration = Integer.parseInt(split[2]);
        } catch (NumberFormatException exception) {
            Logging.error("Potion effect duration " + split[2] + " is not valid.");
            return null;
        }
        return new PotionEffect(
            type,
            duration * 20,
            amplifier - 1,
            false
        );
    }

}
