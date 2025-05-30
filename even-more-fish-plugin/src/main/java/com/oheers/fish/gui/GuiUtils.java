package com.oheers.fish.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.GuiConfig;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.gui.guis.BaitsGui;
import com.oheers.fish.gui.guis.MainMenuGui;
import com.oheers.fish.gui.guis.SellGui;
import com.oheers.fish.gui.guis.FishJournalGui;
import com.oheers.fish.selling.SellHelper;
import com.oheers.fish.items.ItemFactory;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class GuiUtils {

    public static GuiPageElement getFirstPageButton() {
        YamlDocument config = GuiConfig.getInstance().getConfig();
        return new GuiPageElement('f',
            createItemStack(config.getSection("general.first-page")),
            GuiPageElement.PageAction.FIRST
        );
    }

    public static GuiPageElement getNextPageButton() {
        YamlDocument config = GuiConfig.getInstance().getConfig();
        return new GuiPageElement('n',
            createItemStack(config.getSection("general.next-page")),
            GuiPageElement.PageAction.NEXT
        );
    }

    public static GuiPageElement getPreviousPageButton() {
        YamlDocument config = GuiConfig.getInstance().getConfig();
        return new GuiPageElement('p',
            createItemStack(config.getSection("general.previous-page")),
            GuiPageElement.PageAction.PREVIOUS
        );
    }

    public static GuiPageElement getLastPageButton() {
        YamlDocument config = GuiConfig.getInstance().getConfig();
        return new GuiPageElement('l',
            createItemStack(config.getSection("general.last-page")),
            GuiPageElement.PageAction.LAST
        );
    }

    public static ItemStack createItemStack(@Nullable Section section) {
        if (section == null) {
            ItemStack fallback = new ItemStack(Material.BARRIER);
            fallback.editMeta(meta -> meta.displayName(Component.text("Invalid Item")));
            return fallback;
        }
        // Fix for me messing up the item config layout - FireML
        if (section.contains("displayname")) {
            section.set("item.displayname", section.get("displayname"));
            section.remove("displayname");
        }
        ItemFactory factory = ItemFactory.itemFactory(section);
        return factory.createItem();
    }

    public static Map<String, BiConsumer<ConfigGui, GuiElement.Click>> getActionMap() {
        Map<String, BiConsumer<ConfigGui, GuiElement.Click>> newActionMap = new HashMap<>();
        // Exiting the main menu should close the Gui
        newActionMap.put("full-exit", (gui, click) -> {
            if (gui != null) {
                gui.doRescue();
            }
            clearHistory(click.getWhoClicked());
        });
        // Exiting a sub-menu should open the main menu
        newActionMap.put("open-main-menu", (gui, click) -> {
            if (gui != null) {
                gui.doRescue();
            }
            new MainMenuGui(click.getWhoClicked()).open();
            clearHistory(click.getWhoClicked());
        });
        // Toggling custom fish should redraw the Gui and leave it at that
        newActionMap.put("fish-toggle", (gui, click) -> {
            if (click.getWhoClicked() instanceof Player player) {
                EvenMoreFish.getInstance().performFishToggle(player);
            }
            click.getGui().draw();
        });
        // The shop action should just open the shop menu
        newActionMap.put("open-shop", (gui, click) -> {
            if (gui != null) {
                gui.doRescue();
            }

            HumanEntity humanEntity = click.getWhoClicked();

            if (!(humanEntity instanceof Player player)) {
                return;
            }
            new SellGui(player, SellGui.SellState.NORMAL, null).open();
            clearHistory(click.getWhoClicked());
        });
        newActionMap.put("show-command-help", (gui, click) -> Bukkit.dispatchCommand(click.getWhoClicked(), "emf help"));
        newActionMap.put("sell-inventory", (gui, click) -> {
            HumanEntity humanEntity = click.getWhoClicked();
            if (!(humanEntity instanceof Player player)) {
                return;
            }
            if (gui instanceof SellGui sellGui) {
                new SellGui(player, SellGui.SellState.CONFIRM, sellGui.getFishInventory()).open();
                return;
            }
            new SellHelper(click.getWhoClicked().getInventory(), player).sellFish();
            clearHistory(click.getWhoClicked());
        });
        newActionMap.put("sell-shop", (gui, click) -> {
            HumanEntity humanEntity = click.getWhoClicked();
            if (gui instanceof SellGui sellGui && humanEntity instanceof Player player) {
                new SellGui(player, SellGui.SellState.CONFIRM, sellGui.getFishInventory()).open();
                return;
            }
            SellHelper.sellInventoryGui(click.getGui(), click.getWhoClicked());
            clearHistory(click.getWhoClicked());
        });
        newActionMap.put("sell-inventory-confirm", (gui, click) -> {
            HumanEntity humanEntity = click.getWhoClicked();
            if (!(humanEntity instanceof Player player)) {
                return;
            }
            new SellHelper(click.getWhoClicked().getInventory(), player).sellFish();
            if (gui != null) {
                gui.doRescue();
            }
            clearHistory(click.getWhoClicked());
        });
        newActionMap.put("sell-shop-confirm", (gui, click) -> {
            SellHelper.sellInventoryGui(click.getGui(), click.getWhoClicked());
            clearHistory(click.getWhoClicked());
        });
        newActionMap.put("open-baits-menu", (gui, click) -> {
            if (gui != null) {
                gui.doRescue();
            }
            new BaitsGui(click.getWhoClicked()).open();
            clearHistory(click.getWhoClicked());
        });
        newActionMap.put("open-journal-menu", (gui, click) -> {
            if (!MainConfig.getInstance().isDatabaseOnline()) {
                return;
            }
            if (gui != null) {
                gui.doRescue();
            }
            new FishJournalGui(click.getWhoClicked(), null).open();
            clearHistory(click.getWhoClicked());
        });
        // Add page actions so third party plugins cannot register their own.
        newActionMap.put("first-page", (gui, click) -> {});
        newActionMap.put("previous-page", (gui, click) -> {});
        newActionMap.put("next-page", (gui, click) -> {});
        newActionMap.put("last-page", (gui, click) -> {});

        return newActionMap;
    }

    /**
     * Closes the given GUI after 1 tick
     */
    private static void clearHistory(HumanEntity entity) {
        InventoryGui.clearHistory(entity);
    }

}