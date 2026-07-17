package com.oheers.fish.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.api.config.ConfigBase;
import com.oheers.fish.config.gui.GuiConfig;
import com.oheers.fish.config.GuiFillerConfig;
import com.oheers.fish.items.ItemFactory;
import com.oheers.fish.messages.EMFSingleMessage;
import com.oheers.fish.messages.abstracted.EMFMessage;
import com.oheers.fish.utils.ItemUtils;
import de.themoep.inventorygui.GuiElement;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import de.themoep.inventorygui.StaticGuiElement;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.function.BiConsumer;

public class ConfigGui {

    // TODO Bring the action map to this class when we switch to a different library
    protected final Map<String, BiConsumer<ConfigGui, GuiElement.Click>> actions = GuiUtils.getActionMap();
    protected final GuiConfig config;
    protected final Player player;
    private final @NotNull Map<String, EMFMessage> replacements = new HashMap<>();

    private InventoryGui gui;
    private InventoryGui.CloseAction closeAction = null;

    public ConfigGui(@NotNull GuiConfig config, @NotNull HumanEntity human) {
        this.config = config;
        if (!(human instanceof Player p)) {
            throw new UnsupportedOperationException("Cannot open ConfigGui for a non-player.");
        }
        // HumanEntity's only subclass is Player, so this is a safe cast
        this.player = p;
    }

    public void addReplacement(@NotNull String variable, @NotNull String replacement) {
        this.replacements.put(variable, EMFSingleMessage.fromString(replacement));
    }

    public void addReplacement(@NotNull String variable, @NotNull Component replacement) {
        this.replacements.put(variable, EMFSingleMessage.of(replacement));
    }

    public void addReplacement(@NotNull String variable, @NotNull EMFSingleMessage replacement) {
        this.replacements.put(variable, replacement);
    }

    public void addReplacements(@NotNull Map<String, EMFSingleMessage> replacements) {
        this.replacements.putAll(replacements);
    }

    public void setCloseAction(@NotNull InventoryGui.CloseAction closeAction) {
        this.closeAction = closeAction;
    }

    public @NotNull InventoryGui getGui() {
        if (this.gui == null) {
            throw new IllegalStateException("ConfigGui#createGui has not been called!");
        }
        return this.gui;
    }

    public Section getGuiConfig() {
        return config.getConfig();
    }

    public void open() {
        getGui().show(this.player);
    }

    public void createGui() {
        InventoryGui gui = new InventoryGui(
            EvenMoreFish.getInstance(),
            config.getTitle().getLegacyMessage(player),
            config.getLayout()
        );

        loadFiller(gui, config.getConfig());
        loadItems(gui, config.getConfig());
        gui.setCloseAction(closeAction);

        this.gui = gui;
    }

    private void loadFiller(@NotNull InventoryGui gui, @NotNull Section config) {
        String fillerStr = config.getString("filler");
        if (fillerStr == null) {
            return;
        }
        Material filler = ItemUtils.getMaterial(fillerStr);
        if (filler == null) {
            return;
        }
        ItemStack item = new ItemStack(filler);
        item.editMeta(meta -> meta.displayName(Component.empty()));
        gui.setFiller(item);
        gui.addElements(GuiFillerConfig.getInstance().getDefaultFillerItems(this));
    }

    private void loadItems(@NotNull InventoryGui gui, @NotNull Section config) {
        config.getRoutesAsStrings(false).forEach(key -> {
            Section itemSection = config.getSection(key);
            if (itemSection == null || !itemSection.contains("item")) {
                return;
            }
            addGuiItem(gui, itemSection);
        });
    }

    protected void addGuiItem(@NotNull InventoryGui gui, @NotNull Section itemSection) {
        char character = FishUtils.getCharFromString(itemSection.getString("character", "#"), '#');
        if (character == '#') {
            return;
        }
        ItemFactory factory = ItemFactory.itemFactory(itemSection);
        ItemStack item = factory.createItem(this.player.getUniqueId(), this.replacements);
        if (item.getType() == Material.AIR) {
            return;
        }
        StaticGuiElement actionElement = new StaticGuiElement(character, item, click -> {
            executeClickAction(itemSection, click);
            executeClickCommands(itemSection, click);
            return true;
        });
        gui.addElement(actionElement);
    }

    private void executeClickAction(@NotNull Section section, @NotNull GuiElement.Click click) {
        Section actionSection = section.getSection("click-action");
        // If not a section, fall back to a string.
        if (actionSection == null) {
            BiConsumer<ConfigGui, GuiElement.Click> action = actions.get(section.getString("click-action", ""));
            if (action != null) {
                action.accept(this, click);
            }
            return;
        }
        // If a section, filter for the specific click type.
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
    }

    private void executeClickCommands(@NotNull Section section, @NotNull GuiElement.Click click) {
        HumanEntity player = click.getWhoClicked();
        for (String command : section.getStringList("click-commands")) {
            Bukkit.dispatchCommand(player, command);
        }
    }

    public void doRescue() {
        gui.getElements().forEach(element -> {
            if (!(element instanceof GuiStorageElement storageElement)) {
                return;
            }
            Inventory inv = storageElement.getStorage();
            if (inv.isEmpty()) {
                return;
            }
            FishUtils.giveItems(inv.getStorageContents(), player);
            inv.clear();
        });
    }

}
