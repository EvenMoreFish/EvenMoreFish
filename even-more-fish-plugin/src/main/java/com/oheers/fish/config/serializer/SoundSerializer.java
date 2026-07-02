package com.oheers.fish.config.serializer;

import com.oheers.fish.api.Logging;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.sound.Sound;
import org.bukkit.NamespacedKey;
import org.jspecify.annotations.Nullable;

public class SoundSerializer {

    public static String serialize(Sound element) {
        if (element == null) {
            return null;
        }
        return element.name().asString() + "," + element.volume() + "," + element.pitch();
    }

    public static Sound deserialize(String element) {
        return deserialize(element, 1, 1);
    }

    public static Sound deserialize(String element, float defaultVolume, float defaultPitch) {
        if (element == null) {
            return null;
        }
        String[] split = element.split(",");
        if (split.length == 0) {
            return null;
        }
        Sound.Builder sound = resolveSoundType(split[0]);
        if (sound == null) {
            Logging.warn(split[0] + " is not a valid sound.");
            return null;
        }
        try {
            sound.volume(Float.parseFloat(split[1]));
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            sound.volume(defaultVolume);
        }
        try {
            sound.pitch(Float.parseFloat(split[2]));
        } catch (NumberFormatException | ArrayIndexOutOfBoundsException ex) {
            sound.pitch(defaultPitch);
        }
        return sound.build();
    }

    private static Sound.@Nullable Builder resolveSoundType(@Nullable String type) {
        if (type == null) {
            return null;
        }
        Key soundKey = NamespacedKey.fromString(type);
        if (soundKey != null) {
            return Sound.sound().type(soundKey);
        }
        try {
            org.bukkit.Sound soundEnum = org.bukkit.Sound.valueOf(type);
            return Sound.sound().type(soundEnum);
        } catch (IllegalArgumentException exception) {
            return null;
        }
    }

}
