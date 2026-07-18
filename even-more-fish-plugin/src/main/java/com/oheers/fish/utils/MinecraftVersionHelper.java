package com.oheers.fish.utils;

import org.bukkit.Bukkit;
import org.jetbrains.annotations.NotNull;

public class MinecraftVersionHelper {

    public static boolean isAtLeastVersion(@NotNull String versionStr) {
        String currentVersion = Bukkit.getMinecraftVersion();
        return VersionChecker.isNewerThanOrEqualTo(currentVersion, versionStr);
    }

}
