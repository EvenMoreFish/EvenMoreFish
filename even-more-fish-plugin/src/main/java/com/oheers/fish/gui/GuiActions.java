package com.oheers.fish.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.commands.MainCommandProvider;
import com.oheers.fish.database.DatabaseUtil;
import com.oheers.fish.gui.guis.BaitsGui;
import com.oheers.fish.gui.guis.FishJournalGui;
import com.oheers.fish.gui.guis.MainMenuGui;
import com.oheers.fish.gui.guis.SellGui;
import com.oheers.fish.messages.ConfigMessage;
import com.oheers.fish.selling.SellHelper;
import dev.triumphteam.gui.guis.PaginatedGui;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;

public class GuiActions {

    private final Map<String, Consumer<InventoryClickEvent>> actionMap = new HashMap<>();

    public GuiActions(@NotNull EMFGui gui) {
        actionMap.put("close", event -> gui.player.closeInventory());
        actionMap.put("full-exit", event -> gui.player.closeInventory());
        actionMap.put("open-main-menu", event -> new MainMenuGui(gui.player).open());
        actionMap.put("fish-toggle", event -> EvenMoreFish.getInstance().getToggle().performFishToggle(gui.player));
        actionMap.put("open-shop", event -> new SellGui(gui.player, SellGui.SellState.NORMAL, null).open());
        actionMap.put("show-command-help", event -> MainCommandProvider.sendHelpMessage(gui.player));
        actionMap.put("sell-inventory", event -> {
            Player player = gui.player;
            if (gui instanceof SellGui sell) {
                new SellGui(player, SellGui.SellState.CONFIRM, sell.gui.getInventory()).open();;
                return;
            }
            new SellHelper(player.getInventory(), player).sell();
            player.closeInventory();
        });
        actionMap.put("sell-inventory-confirm", event -> {
            Player player = gui.player;
            new SellHelper(player.getInventory(), player).sell();
            player.closeInventory();
        });
        actionMap.put("sell-shop", event -> {
            Player player = gui.player;
            if (gui instanceof SellGui sell) {
                new SellGui(player, SellGui.SellState.CONFIRM, sell.gui.getInventory()).open();
                return;
            }
            new SellHelper(gui.gui.getInventory(), player).sell();
            player.closeInventory();
        });
        actionMap.put("sell-shop-confirm", event -> {
            Player player = gui.player;
            new SellHelper(gui.gui.getInventory(), player).sell();
            player.closeInventory();
        });
        actionMap.put("open-baits-menu", event -> new BaitsGui(gui.player).open());
        actionMap.put("open-journal-menu", event -> {
            if (!DatabaseUtil.isDatabaseOnline()) {
                ConfigMessage.JOURNAL_DISABLED.getMessage().send(gui.player);
                return;
            }
            new FishJournalGui(gui.player, null).open();
        });
        actionMap.put("first-page", event -> {
            if (gui.gui instanceof PaginatedGui paginated) {
                paginated.setPageNum(1);
            }
        });
        actionMap.put("last-page", event -> {
            if (gui.gui instanceof PaginatedGui paginated) {
                paginated.setPageNum(paginated.getPagesNum());
            }
        });
        actionMap.put("next-page", event -> {
            if (gui.gui instanceof PaginatedGui paginated) {
                paginated.next();
            }
        });
        actionMap.put("previous-page", event -> {
            if (gui.gui instanceof PaginatedGui paginated) {
                paginated.previous();
            }
        });
    }

    public void addAction(@NotNull String name, @NotNull Consumer<@NotNull InventoryClickEvent> action) {
        actionMap.putIfAbsent(name, action);
    }

    public @Nullable Consumer<@NotNull InventoryClickEvent> getAction(@NotNull String name) {
        return actionMap.get(name);
    }

}
