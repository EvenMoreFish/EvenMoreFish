package com.oheers.fish.utils;

import com.oheers.fish.FishUtils;
import org.bukkit.Material;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

public class ItemUtils {

    public static @NonNull Material getMaterial(@Nullable String materialName, @NonNull Material defaultMaterial) {
        Material material = getMaterial(materialName);
        if (material == null) {
            return defaultMaterial;
        }
        return material;
    }

    public static @Nullable Material getMaterial(@Nullable String materialName) {
        if (materialName == null) {
            return null;
        }
        return FishUtils.getEnumValue(Material.class, materialName);
    }

    public static boolean isValidMaterial(@Nullable String materialName) {
        if (materialName == null || materialName.isEmpty()) {
            return false;
        }
        return getMaterial(materialName) != null;
    }

}
