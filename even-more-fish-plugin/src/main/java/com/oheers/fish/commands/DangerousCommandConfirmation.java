package com.oheers.fish.commands;

import org.bukkit.command.CommandSender;
import org.jspecify.annotations.NonNull;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class DangerousCommandConfirmation {
    private static final long CONFIRMATION_WINDOW_MILLIS = 30_000L;
    private static final Map<String, Long> pendingConfirmations = new ConcurrentHashMap<>();

    private DangerousCommandConfirmation() {
        throw new UnsupportedOperationException();
    }

    public static boolean confirmOrRequest(
        @NonNull CommandSender sender,
        @NonNull String confirmationId,
        @NonNull String confirmationCommand
    ) {
        long now = System.currentTimeMillis();
        String key = buildKey(sender, confirmationId);
        Long expiresAt = pendingConfirmations.get(key);

        if (expiresAt != null && expiresAt >= now) {
            pendingConfirmations.remove(key);
            return true;
        }

        pendingConfirmations.put(key, now + CONFIRMATION_WINDOW_MILLIS);
        sender.sendMessage(
            "This command permanently deletes EMF database data. Run "
                + confirmationCommand
                + " again within 30 seconds to confirm."
        );
        return false;
    }

    private static @NonNull String buildKey(@NonNull CommandSender sender, @NonNull String confirmationId) {
        return sender.getClass().getName() + ":" + sender.getName() + ":" + confirmationId;
    }
}
