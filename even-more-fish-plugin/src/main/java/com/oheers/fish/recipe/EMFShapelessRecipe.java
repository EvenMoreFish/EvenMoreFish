package com.oheers.fish.recipe;

import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.ShapelessRecipe;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class EMFShapelessRecipe extends EMFRecipe<ShapelessRecipe> {

    private final List<String> ingredients;
    private final NamespacedKey key;
    private final ItemStack result;

    public EMFShapelessRecipe(@NotNull NamespacedKey key, @NotNull ItemStack result, @NotNull List<String> ingredients) {
        if (ingredients.isEmpty()) {
            throw new RuntimeException("Shapeless recipe is missing ingredients.");
        }
        this.ingredients = ingredients;
        this.key = key;
        this.result = result;
    }

    @Override
    protected @NotNull ShapelessRecipe prepareRecipe() {
        ShapelessRecipe recipe = new ShapelessRecipe(key, result);
        this.ingredients.forEach(ingredient -> {
            RecipeChoice choice = RecipeUtil.getRecipeChoice(ingredient);
            if (choice != null) {
                recipe.addIngredient(choice);
            }
        });
        return recipe;
    }

}
