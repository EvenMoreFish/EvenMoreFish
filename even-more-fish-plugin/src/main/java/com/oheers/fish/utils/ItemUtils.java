package com.oheers.fish.utils;

import com.oheers.fish.FishUtils;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ItemUtils {

    public static @NotNull Material getMaterial(@Nullable String materialName, @NotNull Material defaultMaterial) {
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
