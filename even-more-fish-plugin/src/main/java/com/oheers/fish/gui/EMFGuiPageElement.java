package com.oheers.fish.gui;

import de.themoep.inventorygui.GuiPageElement;
import de.themoep.inventorygui.InventoryGui;
import org.bukkit.entity.HumanEntity;
import org.bukkit.inventory.ItemStack;

public class EMFGuiPageElement extends GuiPageElement {

    private final PageAction pageAction;
    private final ItemStack fallbackItem;

    public EMFGuiPageElement(char slotChar, ItemStack item, ItemStack fallbackItem, PageAction pageAction, String... text) {
        super(slotChar, item, pageAction, text);
        this.pageAction = pageAction;
        this.fallbackItem = fallbackItem;
    }

    // Taken from InventoryGui.
    @Override
    public ItemStack getItem(HumanEntity who, int slot) {
        if (((pageAction == PageAction.FIRST || pageAction == PageAction.LAST) && gui.getPageAmount(who) < 3)
            || (pageAction == PageAction.NEXT && gui.getPageNumber(who) + 1 >= gui.getPageAmount(who))
            || (pageAction == PageAction.PREVIOUS && gui.getPageNumber(who) == 0)) {
            return fallbackItem.clone();
        }
        if (pageAction == PageAction.PREVIOUS) {
            setNumber(gui.getPageNumber(who));
        } else if (pageAction == PageAction.NEXT) {
            setNumber(gui.getPageNumber(who) + 2);
        } else if (pageAction == PageAction.LAST) {
            setNumber(gui.getPageAmount(who));
        }
        return super.getItem(who, slot).clone();
    }

}
