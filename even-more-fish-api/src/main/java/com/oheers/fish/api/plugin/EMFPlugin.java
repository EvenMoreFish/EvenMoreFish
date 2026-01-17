package com.oheers.fish.api.plugin;

import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.logging.Level;

public abstract class EMFPlugin extends JavaPlugin {

    private static EMFPlugin instance;

    protected EMFPlugin() {
        if (instance != null) {
            throw new UnsupportedOperationException("EMFPlugin has already been assigned!");
        }
        instance = this;
    }

    public static @NotNull EMFPlugin getInstance() {
        if (instance == null) {
            throw new IllegalStateException("EMFPlugin not found. This should not happen!");
        }
        return instance;
    }

    public void debug(final String message) {
        debug(Level.INFO, message);
    }

    public void debug(final String message, Throwable e) {
        debug(Level.INFO, message, e);
    }

    public void debug(final Level level, final String message) {
        if (isDebugSession()) {
            log(level, "DEBUG %s".formatted(message));
        }
    }

    public void debug(final Level level, final String message, Throwable e) {
        if (isDebugSession()) {
            log(level, "DEBUG %s".formatted(message), e);
        }
    }

    public abstract boolean isDebugSession();

    public abstract void reload(@Nullable CommandSender sender);


    private static void log(final Level level, String message) {
        log(level, message, null);
    }

    private static void log(final Level level, String message, Throwable throwable) {
        StackTraceElement[] stack = Thread.currentThread().getStackTrace();

        String caller = "UnknownSource";
        if (stack.length > 3) {
            StackTraceElement element = stack[3];
            caller = element.getClassName() + "#" + element.getMethodName() + ":" + element.getLineNumber();
        }

        if (throwable == null) {
            getInstance().getLogger().log(level, "[" + caller + "] " + message);
        } else {
            getInstance().getLogger().log(level, "[" + caller + "] " + message, throwable);
        }
    }

}
