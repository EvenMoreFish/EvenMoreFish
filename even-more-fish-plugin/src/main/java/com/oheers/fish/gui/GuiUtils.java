package com.oheers.fish.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.economy.selling.SellHelper;
import com.oheers.fish.commands.MainCommandProvider;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.gui.GuiConfig;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.gui.guis.BaitsGui;
import com.oheers.fish.gui.guis.FishJournalGui;
import com.oheers.fish.gui.guis.MainMenuGui;
import com.oheers.fish.gui.guis.SellGui;
import com.oheers.fish.items.ItemFactory;
import com.oheers.fish.messages.ConfigMessage;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import dev.dejvokep.boostedyaml.YamlDocument;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class GuiUtils {

    public static Map<String, BiConsumer<ConfigGui, GuiElement.Click>> getActionMap() {
        Map<String, BiConsumer<ConfigGui, GuiElement.Click>> newActionMap = new HashMap<>();
        // Exiting the main menu should close the Gui
        newActionMap.put("full-exit", (gui, click) -> {
            if (gui != null) {
                gui.doRescue();
            }
            closeGui(click.getWhoClicked());
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
                EvenMoreFish.getInstance().getToggle().performFishToggle(player);
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
        newActionMap.put("show-command-help", (gui, click) -> {
            click.getWhoClicked().closeInventory();
            MainCommandProvider.sendHelpMessage(click.getWhoClicked());
        });
        newActionMap.put("sell-inventory", (gui, click) -> {
            HumanEntity humanEntity = click.getWhoClicked();
            if (!(humanEntity instanceof Player player)) {
                return;
            }
            if (gui instanceof SellGui sellGui) {
                new SellGui(player, SellGui.SellState.CONFIRM, sellGui.getFishInventory()).open();
                return;
            }
            SellHelper.get().sell(click.getWhoClicked().getInventory(), player);
            closeGui(humanEntity);
        });
        newActionMap.put("sell-shop", (gui, click) -> {
            HumanEntity humanEntity = click.getWhoClicked();
            if (gui instanceof SellGui sellGui && humanEntity instanceof Player player) {
                new SellGui(player, SellGui.SellState.CONFIRM, sellGui.getFishInventory()).open();
                return;
            }
            FishUtils.sellInventoryGui(click.getGui(), click.getWhoClicked());
            closeGui(click.getWhoClicked());
        });
        newActionMap.put("sell-inventory-confirm", (gui, click) -> {
            HumanEntity humanEntity = click.getWhoClicked();
            if (!(humanEntity instanceof Player player)) {
                return;
            }
            SellHelper.get().sell(click.getWhoClicked().getInventory(), player);
            if (gui != null) {
                gui.doRescue();
            }
            closeGui(click.getWhoClicked());
        });
        newActionMap.put("sell-shop-confirm", (gui, click) -> {
            FishUtils.sellInventoryGui(click.getGui(), click.getWhoClicked());
            if (gui != null) {
                gui.doRescue();
            }
            closeGui(click.getWhoClicked());
        });
        newActionMap.put("open-baits-menu", (gui, click) -> {
            if (gui != null) {
                gui.doRescue();
            }
            new BaitsGui(click.getWhoClicked()).open();
            clearHistory(click.getWhoClicked());
        });
        newActionMap.put("open-journal-menu", (gui, click) -> {
            if (!DatabaseUtil.isDatabaseOnline()) {
                ConfigMessage.JOURNAL_DISABLED.getMessage().send(click.getWhoClicked());
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

    private static void closeGui(HumanEntity human) {
        clearHistory(human);
        human.closeInventory();
    }

    private static void clearHistory(HumanEntity human) {
        InventoryGui.clearHistory(human);
    }

}
