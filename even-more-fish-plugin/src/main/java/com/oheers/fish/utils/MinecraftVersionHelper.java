package com.oheers.fish.utils;

import com.oheers.fish.FishUtils;
import de.tr7zw.changeme.nbtapi.utils.MinecraftVersion;
import org.jetbrains.annotations.NotNull;

public class MinecraftVersionHelper {

    public static boolean isAtLeastVersion(@NotNull String versionStr) {
        MinecraftVersion version = FishUtils.getEnumValue(MinecraftVersion.class, versionStr);
        if (version == null) {
            return false;
        }
        return MinecraftVersion.isAtLeastVersion(version);
    }

}
