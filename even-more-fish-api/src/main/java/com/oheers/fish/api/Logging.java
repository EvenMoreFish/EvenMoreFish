package com.oheers.fish.api;

import com.oheers.fish.api.plugin.EMFPlugin;
import net.kyori.adventure.text.Component;

import java.awt.*;

public class Logging {

    public static void info(String message) {
        EMFPlugin.getInstance().getLogger().info(message);
    }

    public static void info(String... message) {
        for (String line : message) {
            info(line);
        }
    }

    public static void info(Component message) {
        EMFPlugin.getInstance().getComponentLogger().info(message);
    }

    public static void info(Component... message) {
        for (Component line : message) {
            info(line);
        }
    }

    public static void warn(String message) {
        EMFPlugin.getInstance().getLogger().warning(message);
    }

    public static void warn(String... message) {
        for (String line : message) {
            warn(line);
        }
    }

    public static void warn(Component message) {
        EMFPlugin.getInstance().getComponentLogger().warn(message);
    }

    public static void warn(Component... message) {
        for (Component line : message) {
            warn(line);
        }
    }

    public static void error(String message) {
        EMFPlugin.getInstance().getLogger().severe(message);
    }

    public static void error(String... message) {
        for (String line : message) {
            error(line);
        }
    }

    public static void error(Component message) {
        EMFPlugin.getInstance().getComponentLogger().error(message);
    }

    public static void error(Component... message) {
        for (Component line : message) {
            error(line);
        }
    }

}
