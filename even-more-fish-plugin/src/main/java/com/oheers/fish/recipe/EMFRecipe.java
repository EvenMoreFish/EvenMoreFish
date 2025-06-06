package com.oheers.fish.recipe;

import org.bukkit.Bukkit;
import org.bukkit.inventory.CraftingRecipe;
import org.jetbrains.annotations.NotNull;

public abstract class EMFRecipe<R extends CraftingRecipe> {

    protected boolean registered = false;
    protected R recipe;

    protected EMFRecipe() {}

    public boolean isRegistered() {
        return this.registered;
    }

    public void register() {
        if (isRegistered()) {
            throw new RuntimeException("Attempted to register a recipe that is already registered.");
        }
        if (recipe == null) {
            this.recipe = prepareRecipe();
        }
        Bukkit.addRecipe(recipe);
        this.registered = true;
    }

    public void unregister() {
        if (!isRegistered()) {
            throw new RuntimeException("Attempted to unregister a recipe that is not registered.");
        }
        Bukkit.removeRecipe(recipe.getKey());
        this.registered = false;
    }

    protected abstract @NotNull R prepareRecipe();

}
