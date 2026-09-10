package com.oheers.fish.api.config.serializer;

import com.oheers.fish.api.Logging;
import io.papermc.paper.registry.RegistryAccess;
import io.papermc.paper.registry.RegistryKey;
import org.bukkit.NamespacedKey;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Locale;

public class PotionEffectSerializer implements EMFSerializer<PotionEffect> {

    private static final PotionEffectSerializer INSTANCE = new PotionEffectSerializer();

    private PotionEffectSerializer() {}

    public static @NonNull PotionEffectSerializer get() {
        return INSTANCE;
    }

    public @NonNull String serialize(@NonNull PotionEffect element) {
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

    public @Nullable PotionEffect deserialize(@Nullable String element, @NonNull String separator) {
        if (element == null) {
            return null;
        }
        String[] split = element.split(separator);
        if (split.length != 3) {
            Logging.error("Invalid potion effect string: " + element);
            Logging.error("The correct format is \"potion,amplifier,duration\".");
            return null;
        }
        NamespacedKey key = NamespacedKey.fromString(split[0].toLowerCase(Locale.ROOT));
        if (key == null) {
            Logging.error("Invalid potion effect key: " + split[0]);
            return null;
        }
        PotionEffectType type = RegistryAccess.registryAccess()
            .getRegistry(RegistryKey.MOB_EFFECT)
            .get(key);
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
