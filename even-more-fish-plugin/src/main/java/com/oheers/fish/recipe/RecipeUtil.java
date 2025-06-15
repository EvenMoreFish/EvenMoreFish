package com.oheers.fish.recipe;

import com.oheers.fish.FishUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class RecipeUtil {

    public static @Nullable RecipeChoice getRecipeChoice(String materialStr) {
        ItemStack customItem = FishUtils.getItem(materialStr);
        if (customItem == null || customItem.getType().isAir()) {
            return null;
        }
        return new RecipeChoice.ExactChoice(customItem);
    }

    public static @Nullable EMFRecipe<?> getRecipe(@NotNull Section section, @NotNull NamespacedKey key, @NotNull ItemStack result) {
        String type = section.getString("type");
        if (type == null) {
            return null;
        }
        return switch (type.toLowerCase()) {
            case "shapeless" -> {
                List<String> ingredients = section.getStringList("ingredients");
                yield new EMFShapelessRecipe(
                    key,
                    result,
                    ingredients
                );
            }
            case "shaped" -> new EMFShapedRecipe(
                key,
                result,
                section
            );
            default -> null; // Not a valid recipe type
        };
    }

}
