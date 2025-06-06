package com.oheers.fish.recipe;

import com.oheers.fish.FishUtils;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapedRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EMFShapedRecipe extends EMFRecipe<ShapedRecipe> {

    private final List<String> rawShape;
    private final Section ingredientsSection;
    private final NamespacedKey key;
    private final ItemStack result;

    public EMFShapedRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, @NotNull Section section) {
        this.rawShape = section.getStringList("shape");
        this.ingredientsSection = section.getSection("ingredients");
        if (this.rawShape.isEmpty() || this.ingredientsSection == null) {
            throw new RuntimeException("Shaped recipe is missing shape or ingredients.");
        }
        this.key = key;
        this.result = result;
    }

    @Override
    protected @NotNull ShapedRecipe prepareRecipe() {
        ShapedRecipe recipe = new ShapedRecipe(key, result);

        String[] shape = rawShape.stream().limit(3).toArray(String[]::new);
        recipe.shape(shape);

        ingredientsSection.getRoutesAsStrings(false).forEach(key -> {
            char character = FishUtils.getCharFromString(key, '#');
            String materialStr = ingredientsSection.getString(key);

            if (materialStr == null) {
                // If invalid material string, just skip it.
                return;
            }

            RecipeChoice choice = RecipeUtil.getRecipeChoice(materialStr);
            if (choice == null) {
                // If a recipe choice could not be created, just skip it.
                return;
            }
            recipe.setIngredient(character, choice);
        });

        return recipe;
    }

}
