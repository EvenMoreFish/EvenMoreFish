package com.oheers.fish.gui;

import com.oheers.fish.FishUtils;
import com.oheers.fish.items.ItemFactory;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.triumphteam.gui.element.GuiItem;
import dev.triumphteam.gui.paper.builder.item.ItemBuilder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public record EMFGuiItem(@NotNull GuiItem<Player, ItemStack> item, char character) {

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
        GuiItem<Player, ItemStack> guiItem = ItemBuilder.from(item).asGuiItem();
        return new EMFGuiItem(guiItem, character);
        /* TODO come back to this once actions are done
        Section actionSection = itemSection.getSection("click-action");
        if (actionSection != null) {
            StaticGuiElement actionElement = new StaticGuiElement(character, item, click -> {
                BiConsumer<ConfigGui, GuiElement.Click> action = switch (click.getType()) {
                    case LEFT -> actions.get(actionSection.getString("left", ""));
                    case RIGHT -> actions.get(actionSection.getString("right", ""));
                    case MIDDLE -> actions.get(actionSection.getString("middle", ""));
                    case DROP -> actions.get(actionSection.getString("drop", ""));
                    default -> null;
                };
                if (action != null) {
                    action.accept(this, click);
                }
                itemSection.getStringList("click-commands").forEach(command ->
                    Bukkit.dispatchCommand(click.getWhoClicked(), command)
                );
                return true;
            });
            gui.addElement(actionElement);
        } else {
            StaticGuiElement element = new StaticGuiElement(character, item, click -> {
                BiConsumer<ConfigGui, GuiElement.Click> action = actions.get(itemSection.getString("click-action", ""));
                if (action != null) {
                    action.accept(this, click);
                }
                itemSection.getStringList("click-commands").forEach(command ->
                    Bukkit.dispatchCommand(click.getWhoClicked(), command)
                );
                return true;
            });
            gui.addElement(element);
        }

         */
    }

}
