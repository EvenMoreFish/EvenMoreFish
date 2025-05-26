package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.gui.ConfigGui;
import com.oheers.fish.gui.GuiUtils;
import com.oheers.fish.items.ItemFactory;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.StaticGuiElement;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;

public class GuiFillerConfig extends ConfigBase {

    private static GuiFillerConfig instance;

    public GuiFillerConfig() {
        super("gui-fillers.yml", "gui-fillers.yml", EvenMoreFish.getInstance(), true);
        instance = this;
    }

    public static GuiFillerConfig getInstance() { return instance; }

    // TODO these were copied from ConfigGui and won't be needed after the switch to Triumph

    public List<GuiElement> getDefaultFillerItems(@NotNull ConfigGui gui) {
        List<GuiElement> elements = new ArrayList<>();
        getConfig().getRoutesAsStrings(false).forEach(key -> {
            Section itemSection = getConfig().getSection(key);
            if (itemSection == null || !itemSection.contains("item")) {
                return;
            }
            StaticGuiElement element = getGuiItem(gui, itemSection);
            if (element != null) {
                elements.add(element);
            }
        });
        return elements;
    }

    private StaticGuiElement getGuiItem(@NotNull ConfigGui gui, @NotNull Section itemSection) {
        char character = FishUtils.getCharFromString(itemSection.getString("character", "#"), '#');
        if (character == '#') {
            return null;
        }
        ItemFactory factory = ItemFactory.itemFactory(itemSection);
        ItemStack item = factory.createItem();
        if (item.getType() == Material.AIR) {
            return null;
        }
        Section actionSection = itemSection.getSection("click-action");
        if (actionSection != null) {
            return new StaticGuiElement(character, item, click -> {
                BiConsumer<ConfigGui, GuiElement.Click> action = switch (click.getType()) {
                    case LEFT -> GuiUtils.getActionMap().get(actionSection.getString("left", ""));
                    case RIGHT -> GuiUtils.getActionMap().get(actionSection.getString("right", ""));
                    case MIDDLE -> GuiUtils.getActionMap().get(actionSection.getString("middle", ""));
                    case DROP -> GuiUtils.getActionMap().get(actionSection.getString("drop", ""));
                    default -> null;
                };
                if (action != null) {
                    action.accept(null, click);
                }
                return true;
            });
        } else {
            return new StaticGuiElement(character, item, click -> {
                BiConsumer<ConfigGui, GuiElement.Click> action = GuiUtils.getActionMap().get(itemSection.getString("click-action", ""));
                if (action != null) {
                    action.accept(gui, click);
                }
                return true;
            });
        }
    }

}
