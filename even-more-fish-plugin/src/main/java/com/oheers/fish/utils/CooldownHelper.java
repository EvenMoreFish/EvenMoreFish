package com.oheers.fish.utils;

import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * An easy way to manage command cooldowns.
 * Taken from DaisyLib.
 */
public class CooldownHelper {

    private final Map<UUID, Instant> cooldownMap;

    private CooldownHelper() {
        cooldownMap = new HashMap<>();
    }

    public static CooldownHelper create() { return new CooldownHelper(); }

    public void applyCooldown(@NonNull UUID uuid, @NonNull Duration duration) {
        cooldownMap.put(uuid, Instant.now().plus(duration));
    }

    public boolean hasCooldown(@NonNull UUID uuid) {
        Instant cooldown = cooldownMap.get(uuid);
        return (cooldown != null && Instant.now().isBefore(cooldown));
    }

    public @Nullable Instant removeCooldown(@NonNull UUID uuid) {
        return cooldownMap.remove(uuid);
    }

    public Duration getRemainingCooldown(@NonNull UUID uuid) {
        Instant cooldown = cooldownMap.get(uuid);
        Instant now = Instant.now();
        if (cooldown != null && now.isBefore(cooldown)) {
            return Duration.between(now, cooldown);
        } else {
            return Duration.ZERO;
        }
    }

}