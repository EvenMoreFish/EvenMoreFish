package com.oheers.fish.utils;

import org.bukkit.Bukkit;
import org.jspecify.annotations.NonNull;

public class MinecraftVersionHelper {

    public static boolean isAtLeastVersion(@NonNull String versionStr) {
        String currentVersion = Bukkit.getMinecraftVersion();
        return VersionChecker.isNewerThanOrEqualTo(currentVersion, versionStr);
    }

}
