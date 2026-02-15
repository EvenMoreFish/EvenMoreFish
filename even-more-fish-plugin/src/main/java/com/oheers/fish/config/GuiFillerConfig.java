package com.oheers.fish.config;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.config.ConfigBase;
import com.oheers.fish.gui.EMFGuiItem;
import com.oheers.fish.items.ItemFactory;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import dev.triumphteam.gui.BaseGui;
import dev.triumphteam.gui.paper.Gui;
import dev.triumphteam.gui.paper.builder.item.ItemBuilder;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Objects;

public class GuiFillerConfig extends ConfigBase {

    private static GuiFillerConfig instance;

    public GuiFillerConfig() {
        super("gui-fillers.yml", "gui-fillers.yml", EvenMoreFish.getInstance(), true);
        instance = this;
    }

    public static GuiFillerConfig getInstance() { return instance; }

    // TODO these were copied from ConfigGui and won't be needed after the switch to Triumph

    public List<EMFGuiItem> getDefaultFillerItems(@NotNull Gui gui) {
        return getConfig().getRoutesAsStrings(false).stream()
            .map(getConfig()::getSection)
            .filter(section -> section != null && section.contains("item"))
            .map(section -> getGuiItem(gui, section))
            .filter(Objects::nonNull)
            .toList();
    }

    private EMFGuiItem getGuiItem(@NotNull Gui gui, @NotNull Section itemSection) {
        char character = FishUtils.getCharFromString(itemSection.getString("character", "#"), '#');
        if (character == '#') {
            return null;
        }
        ItemFactory factory = ItemFactory.itemFactory(itemSection);
        ItemStack item = factory.createItem();
        if (item.getType() == Material.AIR) {
            return null;
        }
        return new EMFGuiItem(ItemBuilder.from(item).asGuiItem(), character);
        /* TODO come back to this once actions are done
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
         */
    }

}
