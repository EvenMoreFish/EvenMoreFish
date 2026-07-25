package com.oheers.fish.api;

import com.oheers.fish.api.plugin.EMFPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.logging.Level;

public class Logging {

    public static void info(@NonNull String message) {
        EMFPlugin.getInstance().getLogger().info(message);
    }

    public static void info(@NonNull String message, @Nullable Throwable throwable) {
        EMFPlugin.getInstance().getLogger().log(Level.INFO, message, throwable);
    }

    public static void info(@NonNull String @NonNull... message) {
        for (String line : message) {
            info(line);
        }
    }

    public static void info(@NonNull Component message) {
        EMFPlugin.getInstance().getComponentLogger().info(message);
    }

    public static void info(@NonNull Component message, @NonNull Throwable throwable) {
        EMFPlugin.getInstance().getComponentLogger().info(message, throwable);
    }

    public static void info(@NonNull Component @NonNull ... message) {
        for (Component line : message) {
            info(line);
        }
    }

    public static void warn(@NonNull String message) {
        EMFPlugin.getInstance().getLogger().warning(message);
    }

    public static void warn(@NonNull String message, @Nullable Throwable throwable) {
        EMFPlugin.getInstance().getLogger().log(Level.WARNING, message, throwable);
    }

    public static void warn(@NonNull String @NonNull ... message) {
        for (String line : message) {
            warn(line);
        }
    }

    public static void warn(@NonNull Component message) {
        EMFPlugin.getInstance().getComponentLogger().warn(message);
    }

    public static void warn(@NonNull Component message, @NonNull Throwable throwable) {
        EMFPlugin.getInstance().getComponentLogger().warn(message, throwable);
    }

    public static void warn(@NonNull Component @NonNull ... message) {
        for (Component line : message) {
            warn(line);
        }
    }

    public static void error(@NonNull String message) {
        EMFPlugin.getInstance().getLogger().severe(message);
    }

    public static void error(@NonNull String message, @Nullable Throwable throwable) {
        EMFPlugin.getInstance().getLogger().log(Level.SEVERE, message, throwable);
    }

    public static void error(@NonNull String @NonNull ... message) {
        for (String line : message) {
            error(line);
        }
    }

    public static void error(@NonNull Component message) {
        EMFPlugin.getInstance().getComponentLogger().error(message);
    }

    public static void error(@NonNull Component message, @NonNull Throwable throwable) {
        EMFPlugin.getInstance().getComponentLogger().error(message, throwable);
    }

    public static void error(@NonNull Component @NonNull ... message) {
        for (Component line : message) {
            error(line);
        }
    }

    public static void debug(@NonNull String message) {
        EMFPlugin.getInstance().debug(message);
    }

    public static void debug(@NonNull String message, @Nullable Throwable throwable) {
        EMFPlugin.getInstance().debug(message, throwable);
    }

    public static void debug(@NonNull String @NonNull ... message) {
        for (String line : message) {
            debug(line);
        }
    }

    public static void debug(@NonNull Component message) {
        String str = PlainTextComponentSerializer.plainText().serialize(message);
        EMFPlugin.getInstance().debug(str);
    }

    public static void debug(@NonNull Component message, @NonNull Throwable throwable) {
        String str = PlainTextComponentSerializer.plainText().serialize(message);
        EMFPlugin.getInstance().debug(str, throwable);
    }

    public static void debug(@NonNull Component @NonNull ... message) {
        for (Component line : message) {
            debug(line);
        }
    }

}
