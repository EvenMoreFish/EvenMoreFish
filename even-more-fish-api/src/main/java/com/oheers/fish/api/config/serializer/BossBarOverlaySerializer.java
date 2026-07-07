package com.oheers.fish.api.config.serializer;

import com.oheers.fish.api.Logging;
import net.kyori.adventure.bossbar.BossBar;
import org.bukkit.boss.BarStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Locale;

public class BossBarOverlaySerializer implements EMFSerializer<BossBar.Overlay> {

    private static final BossBarOverlaySerializer INSTANCE = new BossBarOverlaySerializer();

    private static final BossBar.Overlay DEFAULT = BossBar.Overlay.NOTCHED_10;

    private BossBarOverlaySerializer() {}

    public static @NotNull BossBarOverlaySerializer get() {
        return INSTANCE;
    }

    @Override
    public @NotNull String serialize(@NotNull BossBar.Overlay element) {
        return element.name();
    }

    @Override
    public @NotNull BossBar.Overlay deserialize(@Nullable String element) {
        if (element == null) {
            return DEFAULT;
        }
        element = element.toUpperCase(Locale.ROOT);

        // Modern
        try {
            return BossBar.Overlay.valueOf(element);
        } catch (IllegalArgumentException exception) {
            Logging.debug("Failed to parse modern BossBar.Overlay. Attempting legacy.");
        }

        // Legacy
        try {
            BarStyle legacy = BarStyle.valueOf(element);
            return switch (legacy) {
                case SOLID -> BossBar.Overlay.PROGRESS;
                case SEGMENTED_6 -> BossBar.Overlay.NOTCHED_6;
                case SEGMENTED_10 -> BossBar.Overlay.NOTCHED_10;
                case SEGMENTED_12 -> BossBar.Overlay.NOTCHED_12;
                case SEGMENTED_20 -> BossBar.Overlay.NOTCHED_20;
            };
        } catch (IllegalArgumentException exception) {
            Logging.debug("Failed to parse legacy BarStyle. Returning default.");
            return DEFAULT;
        }
    }

}
