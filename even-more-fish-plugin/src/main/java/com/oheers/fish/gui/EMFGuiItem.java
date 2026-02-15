package com.oheers.fish.gui;

import dev.triumphteam.gui.element.GuiItem;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

public record EMFGuiItem(@NotNull GuiItem<Player, ItemStack> item, char character) {}
