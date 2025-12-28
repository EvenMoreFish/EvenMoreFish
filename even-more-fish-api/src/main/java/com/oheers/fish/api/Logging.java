package com.oheers.fish.api;

import com.oheers.fish.api.plugin.EMFPlugin;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.logging.Level;

public class Logging {

    public static void info(@NotNull String message) {
        EMFPlugin.getInstance().getLogger().info(message);
    }

    public static void info(@NotNull String message, @Nullable Throwable throwable) {
        EMFPlugin.getInstance().getLogger().log(Level.INFO, message, throwable);
    }

    public static void info(@NotNull String @NotNull... message) {
        for (String line : message) {
            info(line);
        }
    }

    public static void info(@NotNull Component message) {
        EMFPlugin.getInstance().getComponentLogger().info(message);
    }

    public static void info(@NotNull Component message, @NotNull Throwable throwable) {
        EMFPlugin.getInstance().getComponentLogger().info(message, throwable);
    }

    public static void info(@NotNull Component @NotNull ... message) {
        for (Component line : message) {
            info(line);
        }
    }

    public static void warn(@NotNull String message) {
        EMFPlugin.getInstance().getLogger().warning(message);
    }

    public static void warn(@NotNull String message, @Nullable Throwable throwable) {
        EMFPlugin.getInstance().getLogger().log(Level.WARNING, message, throwable);
    }

    public static void warn(@NotNull String @NotNull ... message) {
        for (String line : message) {
            warn(line);
        }
    }

    public static void warn(@NotNull Component message) {
        EMFPlugin.getInstance().getComponentLogger().warn(message);
    }

    public static void warn(@NotNull Component message, @NotNull Throwable throwable) {
        EMFPlugin.getInstance().getComponentLogger().warn(message, throwable);
    }

    public static void warn(@NotNull  Component @NotNull ... message) {
        for (Component line : message) {
            warn(line);
        }
    }

    public static void error(@NotNull String message) {
        EMFPlugin.getInstance().getLogger().severe(message);
    }

    public static void error(@NotNull String message, @Nullable Throwable throwable) {
        EMFPlugin.getInstance().getLogger().log(Level.SEVERE, message, throwable);
    }

    public static void error(@NotNull String @NotNull ... message) {
        for (String line : message) {
            error(line);
        }
    }

    public static void error(@NotNull Component message) {
        EMFPlugin.getInstance().getComponentLogger().error(message);
    }

    public static void error(@NotNull Component message, @NotNull Throwable throwable) {
        EMFPlugin.getInstance().getComponentLogger().error(message, throwable);
    }

    public static void error(@NotNull Component @NotNull ... message) {
        for (Component line : message) {
            error(line);
        }
    }

}
