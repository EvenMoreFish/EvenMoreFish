package com.oheers.fish.gui.guis;

import com.oheers.fish.FishUtils;
import com.oheers.fish.api.economy.Economy;
import com.oheers.fish.api.economy.selling.SellHelper;
import com.oheers.fish.config.MainConfig;
import com.oheers.fish.config.gui.GuiConfig;
import com.oheers.fish.config.gui.impl.SellMenuConfirmGuiConfig;
import com.oheers.fish.config.gui.impl.SellMenuNormalGuiConfig;
import com.oheers.fish.gui.EMFConfigGui;
import de.themoep.inventorygui.GuiStorageElement;
import de.themoep.inventorygui.InventoryGui;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.jspecify.annotations.NonNull;
import org.jspecify.annotations.Nullable;

import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

// TODO look into dynamically updating the sell items when a fish is added/removed
public class SellGui extends EMFConfigGui {

    private final Inventory fishInventory;

    public SellGui(@NonNull Player player, @NonNull SellState sellState, @Nullable Inventory fishInventory) {
        super(sellState.getGuiConfig(), player);

        this.fishInventory = Optional.ofNullable(fishInventory).orElse(Bukkit.createInventory(player, 54));

        Economy economy = Economy.getInstance();

        addReplacement("{sell-price}", () -> {
            double shopSaleValue = FishUtils.calculateInventoryWorth(this.fishInventory);
            return economy.getWorthFormat(shopSaleValue, true);
        });
        addReplacement("{sell-all-price}", () -> {
            double playerSaleValue = FishUtils.calculateInventoryWorth(player.getInventory());
            return economy.getWorthFormat(playerSaleValue, true);
        });

        setCloseAction(close -> {
            if (MainConfig.getInstance().sellOverDrop()) {
                SellHelper.get().sell(this.fishInventory, this.player);
            }
            doRescue(close.getGui());
            return false;
        });
    }

    public Inventory getFishInventory() {
        return this.fishInventory;
    }

    @Override
    public void configure(@NonNull InventoryGui inventoryGui) {
        char character = FishUtils.getCharFromString(guiConfig.getConfig().getString("deposit-character", "i"), 'i');
        inventoryGui.addElement(new GuiStorageElement(character, this.fishInventory));
    }

    @Override
    protected void addClickActions(@NonNull Map<String, ConfigGuiClickAction> map) {}

    public enum SellState {
        NORMAL(SellMenuNormalGuiConfig::getInstance),
        CONFIRM(SellMenuConfirmGuiConfig::getInstance);

        private final Supplier<GuiConfig> configSupplier;

        SellState(@NonNull Supplier<GuiConfig> configSupplier) {
            this.configSupplier = configSupplier;
        }

        public GuiConfig getGuiConfig() {
            return configSupplier.get();
        }
    }

}
