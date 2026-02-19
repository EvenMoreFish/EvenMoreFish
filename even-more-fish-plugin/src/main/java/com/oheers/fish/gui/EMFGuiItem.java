package com.oheers.fish.gui;

import com.oheers.fish.FishUtils;
import com.oheers.fish.items.ItemFactory;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.triumphteam.gui.guis.GuiItem;
import org.bukkit.Bukkit;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.function.Consumer;

public record EMFGuiItem(@NotNull GuiItem item, char character) {

    public static @Nullable EMFGuiItem create(@NotNull EMFGui gui, @NotNull Section section) {
        char character = FishUtils.getCharFromString(section.getString("character", "#"), '#');
        if (character == '#') {
            return null;
        }
        ItemFactory factory = ItemFactory.itemFactory(section);
        ItemStack item = factory.createItem(gui.getPlayer().getUniqueId(), gui.getReplacements());
        if (item.isEmpty()) {
            return null;
        }
        GuiItem guiItem = getGuiItem(gui, item, section);
        return new EMFGuiItem(guiItem, character);
    }

    private static GuiItem getGuiItem(@NotNull EMFGui gui, @NotNull ItemStack item, @NotNull Section section) {
        Section actionSection = section.getSection("click-action");
        // If the click-action key is a section, we need to figure out based on the click type.
        if (actionSection != null) {
            return new GuiItem(item, event -> {
                event.setCancelled(true);
                String actionString = switch (event.getClick()) {
                    case LEFT -> actionSection.getString("left", "");
                    case RIGHT -> actionSection.getString("right", "");
                    case MIDDLE -> actionSection.getString("middle", "");
                    case DROP -> actionSection.getString("drop", "");
                    default -> "";
                };
                Consumer<InventoryClickEvent> action = gui.actions.getAction(actionString);
                if (action != null) {
                    action.accept(event);
                }
                section.getStringList("click-commands").forEach(command ->
                    Bukkit.dispatchCommand(event.getWhoClicked(), command)
                );
            });
        }

        // If click-action is not a key, parse the String and perform on any click instead.
        return new GuiItem(item, event -> {
            String actionString = section.getString("click-action", "");
            Consumer<InventoryClickEvent> action = gui.actions.getAction(actionString);
            if (action != null) {
                action.accept(event);
            }
            section.getStringList("click-commands").forEach(command ->
                Bukkit.dispatchCommand(event.getWhoClicked(), command)
            );
        });
    }

}
