package com.oheers.fish.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.config.GUIConfig;
import com.oheers.fish.gui.guis.BaitsGui;
import com.oheers.fish.gui.guis.MainMenuGui;
import com.oheers.fish.gui.guis.SellGui;
import com.oheers.fish.selling.SellHelper;
import com.oheers.fish.utils.ItemBuilder;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiPageElement;
import dev.dejvokep.boostedyaml.YamlDocument;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;

public class GUIUtils {

    public static GuiPageElement getFirstPageButton() {
        YamlDocument config = GUIConfig.getInstance().getConfig();
        return new GuiPageElement('f',
            createItemStack(
                config.getString("gui.global.first-page.material", "arrow"),
                Material.ARROW,
                config.getString("gui.global.first-page.name", "<aqua>First Page"),
                config.getStringList("gui.global.first-page.lore")
            ),
            GuiPageElement.PageAction.FIRST
        );
    }

    public static GuiPageElement getNextPageButton() {
        YamlDocument config = GUIConfig.getInstance().getConfig();
        return new GuiPageElement('n',
            createItemStack(
                config.getString("gui.global.next-page.material", "paper"),
                Material.PAPER,
                config.getString("gui.global.next-page.name", "<aqua>Next Page"),
                config.getStringList("gui.global.next-page.lore")
            ),
            GuiPageElement.PageAction.NEXT
        );
    }

    public static GuiPageElement getPreviousPageButton() {
        YamlDocument config = GUIConfig.getInstance().getConfig();
        return new GuiPageElement('p',
            createItemStack(
                config.getString("gui.global.previous-page.material", "paper"),
                Material.PAPER,
                config.getString("gui.global.previous-page.name", "<aqua>Previous Page"),
                config.getStringList("gui.global.previous-page.lore")
            ),
            GuiPageElement.PageAction.PREVIOUS
        );
    }

    public static GuiPageElement getLastPageButton() {
        YamlDocument config = GUIConfig.getInstance().getConfig();
        return new GuiPageElement('l',
            createItemStack(
                config.getString("gui.global.last-page.material", "arrow"),
                Material.ARROW,
                config.getString("gui.global.last-page.name", "<aqua>Last Page"),
                config.getStringList("gui.global.last-page.lore")
            ),
            GuiPageElement.PageAction.LAST
        );
    }

    public static ItemStack createItemStack(@NotNull String materialName, @NotNull Material defaultMaterial, @NotNull String display, @NotNull List<String> lore) {
        return new ItemBuilder(materialName, defaultMaterial)
            .withDisplay(display)
            .withLore(lore)
            .build();
    }

    public static Map<String, BiConsumer<ConfigGui, GuiElement.Click>> getActionMap() {
        Map<String, BiConsumer<ConfigGui, GuiElement.Click>> newActionMap = new HashMap<>();
        // Exiting the main menu should close the GUI
        newActionMap.put("full-exit", (gui, click) -> {
            if (gui != null) {
                gui.doRescue();
            }
            click.getGui().close();
        });
        // Exiting a sub-menu should open the main menu
        newActionMap.put("open-main-menu", (gui, click) -> {
            if (gui != null) {
                gui.doRescue();
            }
            new MainMenuGui(click.getWhoClicked()).open();
        });
        // Toggling custom fish should redraw the GUI and leave it at that
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
        });
        newActionMap.put("show-command-help", (gui, click) -> Bukkit.dispatchCommand(click.getWhoClicked(), "emf help"));
        newActionMap.put("sell-inventory", (gui, click) -> {
            HumanEntity humanEntity = click.getWhoClicked();
            if (!(humanEntity instanceof Player player)) {
                return;
            }
            if (gui instanceof SellGui sellGUI) {
                new SellGui(player, SellGui.SellState.CONFIRM, sellGUI.getFishInventory()).open();
                return;
            }
            new SellHelper(click.getWhoClicked().getInventory(), player).sellFish();
            click.getGui().close();
        });
        newActionMap.put("sell-shop", (gui, click) -> {
            HumanEntity humanEntity = click.getWhoClicked();
            if (gui instanceof SellGui sellGUI && humanEntity instanceof Player player) {
                new SellGui(player, SellGui.SellState.CONFIRM, sellGUI.getFishInventory()).open();
                return;
            }
            SellHelper.sellInventoryGui(click.getGui(), click.getWhoClicked());
            click.getGui().close();
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
            click.getGui().close();
        });
        newActionMap.put("sell-shop-confirm", (gui, click) -> {
            SellHelper.sellInventoryGui(click.getGui(), click.getWhoClicked());
            click.getGui().close();
        });
        newActionMap.put("open-baits-menu", (gui, click) -> {
            if (gui != null) {
                gui.doRescue();
            }
            new BaitsGui(click.getWhoClicked()).open();
        });
        // Add page actions so third party plugins cannot register their own.
        newActionMap.put("first-page", (gui, click) -> {});
        newActionMap.put("previous-page", (gui, click) -> {});
        newActionMap.put("next-page", (gui, click) -> {});
        newActionMap.put("last-page", (gui, click) -> {});



        return newActionMap;
    }

}