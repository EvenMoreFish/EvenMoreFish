package com.oheers.fish.gui;

import com.oheers.fish.EvenMoreFish;
import com.oheers.fish.FishUtils;
import com.oheers.fish.config.EMFConfigReader;
import com.oheers.fish.config.GuiFillerConfig;
import com.oheers.fish.config.gui.GuiConfig;
import com.oheers.fish.items.ItemFactory;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import dev.dejvokep.boostedyaml.block.implementation.Section;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.jspecify.annotations.NullMarked;

@NullMarked
public abstract class EMFConfigGui extends de.themoep.inventorygui.config.ConfigGui<Section> {

    protected final GuiConfig guiConfig;

    public EMFConfigGui(GuiConfig guiConfig, HumanEntity human) {
        super(human, EvenMoreFish.getInstance(), new EMFConfigReader(guiConfig.getConfig()));
        this.guiConfig = guiConfig;
    }

    public void doRescue(InventoryGui gui) {
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

    @Override
    public Component getTitle() {
        return guiConfig.getTitle().getComponentMessage(player);
    }

    @Override
    public void configureGui(InventoryGui inventoryGui) {
        loadItems(inventoryGui, GuiFillerConfig.getInstance().getSupplier()); // Loads filler items.
        configure(inventoryGui);
    }

    public abstract void configure(InventoryGui inventoryGui);

    @Override
    protected ItemStack buildItem(Section section) {
        return ItemFactory.itemFactory(section, null, null).createItem(player.getUniqueId(), replacements);
    }

}
